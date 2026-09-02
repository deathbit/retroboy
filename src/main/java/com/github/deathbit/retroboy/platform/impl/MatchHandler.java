package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.domain.MatchPairForGame;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.match.MatchStrategy;
import com.github.deathbit.retroboy.match.strategy.FullExactMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.FuzzyRatioMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.NoSpaceMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.PartialExactMatchStrategy;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public class MatchHandler {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    /**
     * 漏斗策略链（前三层），由紧到松依次执行
     */
    private static final List<MatchStrategy> STRATEGIES = List.of(
        new FullExactMatchStrategy(),
        new PartialExactMatchStrategy(),
        new NoSpaceMatchStrategy()
    );

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    public void handle(PlatformContext platformContext) throws Exception {
        var wikiList = new ArrayList<>(platformContext.getWikiGamePackages());
        var gameList = new ArrayList<>(platformContext.getNoIntroGamePackages());
        var areaMapping = getPlatformProcessor(platformContext).gameDBToWikiDBAreaMapping(
            buildGameAreas(gameList), buildWikiAreas(wikiList));

        var allPairs = new ArrayList<MatchPairForPackage>();

        // 前三层漏斗
        for (var strategy : STRATEGIES) {
            allPairs.addAll(strategy.match(wikiList, gameList, areaMapping));
        }

        // 第四层：WeightedRatio 模糊匹配（跳过 noFuzzyRatioMatch 中指定的 wiki 包）
        var noFuzzy = platformContext.getPlatformPackTaskConfig().getNoFuzzyRatioMatch();
        var skippedWiki = new ArrayList<WikiGamePackage>();
        if (noFuzzy != null && !noFuzzy.isEmpty()) {
            wikiList.removeIf(pkg -> {
                if (noFuzzy.contains(pkg.getId())) {
                    skippedWiki.add(pkg);
                    return true;
                }
                return false;
            });
        }
        var fuzzyStrategy = new FuzzyRatioMatchStrategy();
        allPairs.addAll(fuzzyStrategy.match(wikiList, gameList, areaMapping));
        wikiList.addAll(skippedWiki); // 放回，进入后续 direct mapping 或 mismatch

        // 第五层：配置文件直接映射（最后兜底，目标 ID 必须存在且未被使用）
        var mappingList = platformContext.getPlatformPackTaskConfig().getPackageMappingList();
        if (mappingList != null && !mappingList.isEmpty()) {
            allPairs.addAll(applyDirectMapping(mappingList, wikiList, gameList));
        }

        // 按 MatchLevel 分类（保证所有 level 均有 key）
        var matchPairsByLevel = new EnumMap<MatchLevel, List<MatchPairForPackage>>(MatchLevel.class);
        for (var level : MatchLevel.values()) {
            matchPairsByLevel.put(level, new ArrayList<>());
        }
        allPairs.forEach(pair -> matchPairsByLevel.get(pair.getMatchLevel()).add(pair));

        platformContext.setMatchResult(MatchResult.builder()
                                                  .gameDBToWikiDBAreaMapping(areaMapping)
                                                  .matchPairsByLevel(matchPairsByLevel)
                                                  .mismatchWikiGamePackages(wikiList)
                                                  .unusedNoIntroGamePackages(gameList)
                                                  .fuzzyMatchDetails(fuzzyStrategy.getFuzzyMatchDetails())
                                                  .build());
        generateMatchReport(platformContext);
        platformContext.setMatchPairForGamesByArea(buildMatchPairForGamesByArea(platformContext.getMatchResult(), areaMapping));
    }

    private List<String> buildWikiAreas(List<WikiGamePackage> wikiGamePackages) {
        var areas = new LinkedHashSet<String>();
        for (var pkg : wikiGamePackages) {
            areas.addAll(pkg.getWikiGameByArea().keySet());
        }
        return new ArrayList<>(areas);
    }

    private List<String> buildGameAreas(List<NoIntroGamePackage> noIntroGamePackages) {
        var areas = new LinkedHashSet<String>();
        for (var pkg : noIntroGamePackages) {
            areas.addAll(pkg.getNoIntroGameByArea().keySet());
        }
        return new ArrayList<>(areas);
    }

    /**
     * 按配置的 "wikiId -> gameId" 列表做直接映射。
     * 条件：两端 ID 均存在于剩余列表中（即未被前几层消耗），满足则配对并从列表移除。
     */
    private List<MatchPairForPackage> applyDirectMapping(List<String> mappingList,
        List<WikiGamePackage> wikiList,
        List<NoIntroGamePackage> gameList
    ) {
        // 校验配置层重复：同一 wiki ID 或 game ID 不得出现多次
        var seenWikiIds = new LinkedHashSet<String>();
        var seenGameIds = new LinkedHashSet<String>();
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            if (parts.length != 2) {
                throw new RuntimeException("packageMappingList 格式错误: " + mapping);
            }
            var wikiId = parts[0].trim();
            var gameId = parts[1].trim();
            if (!seenWikiIds.add(wikiId)) {
                throw new RuntimeException("packageMappingList 中 wiki ID 重复: " + wikiId);
            }
            if (!seenGameIds.add(gameId)) {
                throw new RuntimeException("packageMappingList 中 game ID 重复: " + gameId);
            }
        }

        var pairs = new ArrayList<MatchPairForPackage>();
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            var wikiId = parts[0].trim();
            var gameId = parts[1].trim();

            WikiGamePackage wikiPkg = null;
            for (var pkg : wikiList) {
                if (wikiId.equals(pkg.getId())) {
                    wikiPkg = pkg;
                    break;
                }
            }
            NoIntroGamePackage gamePkg = null;
            for (var pkg : gameList) {
                if (gameId.equals(pkg.getId())) {
                    gamePkg = pkg;
                    break;
                }
            }
            // 运行层校验：ID 必须存在且未被前几层消耗
            if (wikiPkg == null) {
                throw new RuntimeException("packageMappingList: wiki ID 不存在或已被使用: " + wikiId);
            }
            if (gamePkg == null) {
                throw new RuntimeException("packageMappingList: game ID 不存在或已被使用: " + gameId);
            }

            pairs.add(MatchPairForPackage.builder()
                                         .wikiGamePackage(wikiPkg)
                                         .noIntroGamePackage(gamePkg)
                                         .matchLevel(MatchLevel.DIRECT_MAPPING)
                                         .build());
            wikiList.remove(wikiPkg);
            gameList.remove(gamePkg);
        }
        return pairs;
    }

    private void generateMatchReport(PlatformContext platformContext) {
        var matchResult = platformContext.getMatchResult();
        var platform = platformContext.getPlatform().getName();

        var report = new LinkedHashMap<String, Object>();
        for (var level : MatchLevel.values()) {
            var pairs = matchResult.getMatchPairsByLevel().getOrDefault(level, List.of());
            report.put(level.name().toLowerCase(), buildPairReport(pairs));
        }
        report.put("mismatchWikiDBPackages", buildWikiPackageReport(matchResult.getMismatchWikiGamePackages()));
        report.put("unusedGameDBPackages", buildGamePackageReport(matchResult.getUnusedNoIntroGamePackages()));

        var outputPath = Path.of("src/main/resources/platform/" + platform + "/" + platform + "_wiki_db.json");
        try {
            Files.writeString(outputPath, GSON.toJson(report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!matchResult.getMismatchWikiGamePackages().isEmpty()) {
            throw new RuntimeException("mismatchWikiDBPackages is not empty: " + matchResult.getMismatchWikiGamePackages().size() + " unmatched wiki packages");
        }
        if (!matchResult.getUnusedNoIntroGamePackages().isEmpty()) {
            throw new RuntimeException("unusedGameDBPackages is not empty: " + matchResult.getUnusedNoIntroGamePackages().size() + " unused game packages");
        }
    }

    private Map<String, List<MatchPairForGame>> buildMatchPairForGamesByArea(
        MatchResult matchResult,
        Map<String, String> areaMapping
    ) {
        var matchPairForGamesByArea = new LinkedHashMap<String, List<MatchPairForGame>>();

        for (var pairs : matchResult.getMatchPairsByLevel().values()) {
            for (var pair : pairs) {
                var gameDBByWikiArea = buildGameDBByWikiArea(pair.getNoIntroGamePackage(), areaMapping);
                for (var entry : pair.getWikiGamePackage().getWikiGameByArea().entrySet()) {
                    var wikiArea = entry.getKey();
                    var wikiDB = entry.getValue();
                    var gameDB = gameDBByWikiArea.get(wikiArea);
                    if (gameDB == null) {
                        throw new RuntimeException("MatchPairForGame 缺少对应 GameDB: wikiPackageId=%s, gamePackageId=%s, area=%s"
                            .formatted(pair.getWikiGamePackage().getId(), pair.getNoIntroGamePackage().getId(), wikiArea));
                    }
                    matchPairForGamesByArea.computeIfAbsent(wikiArea, ignored -> new ArrayList<>())
                                           .add(MatchPairForGame.builder()
                                                                .wikiGame(wikiDB)
                                                                .noIntroGame(gameDB)
                                                                .build());
                }
            }
        }

        return matchPairForGamesByArea;
    }

    private Map<String, NoIntroGame> buildGameDBByWikiArea(
        NoIntroGamePackage noIntroGamePackage,
        Map<String, String> areaMapping
    ) {
        var gameDBByWikiArea = new LinkedHashMap<String, NoIntroGame>();
        for (var entry : noIntroGamePackage.getNoIntroGameByArea().entrySet()) {
            var gameArea = entry.getKey();
            var wikiArea = areaMapping.getOrDefault(gameArea, gameArea);
            var existingGameDB = gameDBByWikiArea.get(wikiArea);
            if (existingGameDB != null) {
                throw new RuntimeException("MatchPairForGame area conflict: gamePackageId=%s, wikiArea=%s, existing=%s(id=%s), duplicate=%s(id=%s)"
                    .formatted(
                        noIntroGamePackage.getId(),
                        wikiArea,
                        existingGameDB.getTitle(),
                        existingGameDB.getId(),
                        entry.getValue().getTitle(),
                        entry.getValue().getId()
                    ));
            }
            gameDBByWikiArea.put(wikiArea, entry.getValue());
        }
        return gameDBByWikiArea;
    }

    private List<Map<String, Object>> buildPairReport(List<MatchPairForPackage> pairs) {
        return pairs.stream().map(pair -> {
            var entry = new LinkedHashMap<String, Object>();
            entry.put("wiki", extractWikiNames(pair.getWikiGamePackage()));
            entry.put("game", extractGameNames(pair.getNoIntroGamePackage()));
            return (Map<String, Object>) entry;
        }).toList();
    }

    private List<Map<String, String>> buildWikiPackageReport(List<WikiGamePackage> packages) {
        if (packages == null) {
            return List.of();
        }
        return packages.stream().map(pkg -> {
            var entry = new LinkedHashMap<String, String>();
            entry.put("id", pkg.getId());
            pkg.getWikiGameByArea().forEach((area, wikiDB) -> entry.put(area, wikiDB.getTitle()));
            return (Map<String, String>) entry;
        }).toList();
    }

    private List<Map<String, String>> buildGamePackageReport(List<NoIntroGamePackage> packages) {
        if (packages == null) {
            return List.of();
        }
        return packages.stream().map(pkg -> {
            var entry = new LinkedHashMap<String, String>();
            entry.put("id", pkg.getId());
            pkg.getNoIntroGameByArea().forEach((area, gameDB) -> entry.put(area, area + " - " + gameDB.getTitle()));
            return (Map<String, String>) entry;
        }).toList();
    }

    private Map<String, String> extractWikiNames(WikiGamePackage pkg) {
        var names = new LinkedHashMap<String, String>();
        pkg.getWikiGameByArea().forEach((area, wikiDB) -> names.put(area, wikiDB.getTitle()));
        return names;
    }

    private Map<String, String> extractGameNames(NoIntroGamePackage pkg) {
        var names = new LinkedHashMap<String, String>();
        pkg.getNoIntroGameByArea().forEach((area, gameDB) -> names.put(area, gameDB.getTitle()));
        return names;
    }

    private PlatformProcessor getPlatformProcessor(PlatformContext platformContext) {
        var platformProcessor = platformProcessorMap.get(platformContext.getPlatform());
        if (platformProcessor == null) {
            throw new IllegalStateException("PlatformProcessor not found for platform: " + platformContext.getPlatform());
        }
        return platformProcessor;
    }
}

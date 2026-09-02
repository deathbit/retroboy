package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.MatchPairForGame;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.match.MatchStrategy;
import com.github.deathbit.retroboy.match.strategy.FullExactMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.FuzzyRatioMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.NoSpaceMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.PartialExactMatchStrategy;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.FileContextUtils;
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
import java.util.Locale;
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

        generateMatchReport(platformContext, matchPairsByLevel, wikiList, gameList);
        platformContext.setMatchResults(buildMatchResults(platformContext, allPairs, areaMapping));
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

    private void generateMatchReport(
            PlatformContext platformContext,
            Map<MatchLevel, List<MatchPairForPackage>> matchPairsByLevel,
            List<WikiGamePackage> mismatchWikiGamePackages,
            List<NoIntroGamePackage> unusedNoIntroGamePackages
    ) {
        var platform = platformContext.getPlatform().getName();

        var report = new LinkedHashMap<String, Object>();
        for (var level : MatchLevel.values()) {
            var pairs = matchPairsByLevel.getOrDefault(level, List.of());
            report.put(level.name().toLowerCase(), buildPairReport(pairs));
        }
        report.put("mismatchWikiDBPackages", buildWikiPackageReport(mismatchWikiGamePackages));
        report.put("unusedGameDBPackages", buildGamePackageReport(unusedNoIntroGamePackages));

        var outputPath = Path.of("src/main/resources/platform/" + platform + "/" + platform + "_wiki_db.json");
        try {
            Files.writeString(outputPath, GSON.toJson(report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!mismatchWikiGamePackages.isEmpty()) {
            throw new RuntimeException("mismatchWikiDBPackages is not empty: " + mismatchWikiGamePackages.size() + " unmatched wiki packages");
        }
        if (!unusedNoIntroGamePackages.isEmpty()) {
            throw new RuntimeException("unusedGameDBPackages is not empty: " + unusedNoIntroGamePackages.size() + " unused game packages");
        }
    }

    private List<MatchResult> buildMatchResults(
            PlatformContext platformContext,
            List<MatchPairForPackage> pairs,
            Map<String, String> areaMapping
    ) {
        var fileContextLookupMap = FileContextUtils.buildLookupMap(platformContext.getFileContexts());
        var ssGamePackagesBySha1 = buildSSGamePackagesBySha1(platformContext.getSsGamePackages());
        var matchResults = new ArrayList<MatchResult>();

        for (var pair : pairs) {
            var gameDBByWikiArea = buildGameDBByWikiArea(pair.getNoIntroGamePackage(), areaMapping);
            var matchedWikiAreas = new LinkedHashSet<String>();
            var matchPairForGameByArea = new LinkedHashMap<String, MatchPairForGame>();
            var fileContextByArea = new LinkedHashMap<String, FileContext>();
            var ssGamePackageByArea = new LinkedHashMap<String, SSGamePackage>();

            for (var entry : pair.getNoIntroGamePackage().getNoIntroGameByArea().entrySet()) {
                var noIntroArea = entry.getKey();
                var noIntroGame = entry.getValue();
                var wikiArea = areaMapping.getOrDefault(noIntroArea, noIntroArea);
                var wikiGame = pair.getWikiGamePackage().getWikiGameByArea().get(wikiArea);
                if (wikiGame == null) {
                    throw new RuntimeException("MatchResult 缺少对应 WikiDB: wikiPackageId=%s, gamePackageId=%s, gameArea=%s, wikiArea=%s"
                            .formatted(pair.getWikiGamePackage().getId(), pair.getNoIntroGamePackage().getId(), noIntroArea, wikiArea));
                }
                matchedWikiAreas.add(wikiArea);

                var fileContext = FileContextUtils.requireFileContext(fileContextLookupMap, noIntroGame.getTitle());
                var ssGamePackage = requireSingleSSGamePackage(ssGamePackagesBySha1, fileContext, noIntroArea, noIntroGame.getTitle());

                matchPairForGameByArea.put(noIntroArea, MatchPairForGame.builder()
                        .wikiGame(wikiGame)
                        .noIntroGame(noIntroGame)
                        .build());
                fileContextByArea.put(noIntroArea, fileContext);
                ssGamePackageByArea.put(noIntroArea, ssGamePackage);
            }

            for (var wikiArea : pair.getWikiGamePackage().getWikiGameByArea().keySet()) {
                if (!matchedWikiAreas.contains(wikiArea)) {
                    var gameDB = gameDBByWikiArea.get(wikiArea);
                    throw new RuntimeException("MatchResult 缺少对应 GameDB: wikiPackageId=%s, gamePackageId=%s, wikiArea=%s"
                            .formatted(pair.getWikiGamePackage().getId(), pair.getNoIntroGamePackage().getId(), wikiArea));
                }
            }

            validateSingleSSGamePackageByArea(platformContext, pair, matchPairForGameByArea, fileContextByArea, ssGamePackageByArea);
            matchResults.add(MatchResult.builder()
                    .wikiGamePackage(pair.getWikiGamePackage())
                    .noIntroGamePackage(pair.getNoIntroGamePackage())
                    .matchPairForGameByArea(matchPairForGameByArea)
                    .fileContextByArea(fileContextByArea)
                    .ssGamePackageByArea(ssGamePackageByArea)
                    .build());
        }

        return matchResults;
    }

    private void validateSingleSSGamePackageByArea(
            PlatformContext platformContext,
            MatchPairForPackage pair,
            Map<String, MatchPairForGame> matchPairForGameByArea,
            Map<String, FileContext> fileContextByArea,
            Map<String, SSGamePackage> ssGamePackageByArea
    ) {
        var allowList = platformContext.getPlatformPackTaskConfig().getAllowDifferentSSGamePackageWikiIds();
        if (allowList != null && allowList.contains(pair.getWikiGamePackage().getId())) {
            return;
        }

        String expectedPackageId = null;
        var actualPackageDetailsByArea = new LinkedHashMap<String, Map<String, Object>>();
        for (var entry : ssGamePackageByArea.entrySet()) {
            var area = entry.getKey();
            var ssGamePackage = entry.getValue();
            var packageId = ssGamePackage.getId();
            actualPackageDetailsByArea.put(area, buildSSGamePackageConflictDetail(
                    matchPairForGameByArea.get(area),
                    fileContextByArea.get(area),
                    ssGamePackage
            ));
            if (expectedPackageId == null) {
                expectedPackageId = packageId;
                continue;
            }
            if (!expectedPackageId.equals(packageId)) {
                throw new RuntimeException("MatchResult SSGamePackage area conflict:\n" + buildSSGamePackageConflictJson(pair, actualPackageDetailsByArea));
            }
        }
    }

    private String buildSSGamePackageConflictJson(
            MatchPairForPackage pair,
            Map<String, Map<String, Object>> detailsByArea
    ) {
        var report = new LinkedHashMap<String, Object>();
        report.put("wikiPackageId", pair.getWikiGamePackage().getId());
        report.put("gamePackageId", pair.getNoIntroGamePackage().getId());
        report.put("detailsByArea", detailsByArea);
        return GSON.toJson(report);
    }

    private Map<String, Object> buildSSGamePackageConflictDetail(
            MatchPairForGame matchPairForGame,
            FileContext fileContext,
            SSGamePackage ssGamePackage
    ) {
        var detail = new LinkedHashMap<String, Object>();
        detail.put("wikiGame", matchPairForGame == null ? null : matchPairForGame.getWikiGame().getTitle());
        detail.put("noIntroGame", matchPairForGame == null ? null : matchPairForGame.getNoIntroGame().getTitle());
        detail.put("fileName", fileContext == null ? null : fileContext.getFileName());
        detail.put("sha1", fileContext == null ? null : fileContext.getSha1());
        detail.put("ssPackageId", ssGamePackage == null ? null : ssGamePackage.getId());
        detail.put("ssGames", buildSSGameNamesByArea(ssGamePackage));
        return detail;
    }

    private Map<String, String> buildSSGameNamesByArea(SSGamePackage ssGamePackage) {
        var ssGameNamesByArea = new LinkedHashMap<String, String>();
        if (ssGamePackage == null || ssGamePackage.getSsGameByArea() == null) {
            return ssGameNamesByArea;
        }
        ssGamePackage.getSsGameByArea().forEach((area, ssGame) -> ssGameNamesByArea.put(area, ssGame.getTitle()));
        return ssGameNamesByArea;
    }

    private Map<String, List<SSGamePackage>> buildSSGamePackagesBySha1(List<SSGamePackage> ssGamePackages) {
        var lookupMap = new LinkedHashMap<String, List<SSGamePackage>>();
        if (ssGamePackages == null) {
            return lookupMap;
        }
        for (var ssGamePackage : ssGamePackages) {
            if (ssGamePackage.getSha1s() == null) {
                continue;
            }
            for (var sha1 : ssGamePackage.getSha1s()) {
                if (sha1 == null || sha1.isBlank()) {
                    continue;
                }
                lookupMap.computeIfAbsent(normalizeSha1(sha1), ignored -> new ArrayList<>()).add(ssGamePackage);
            }
        }
        return lookupMap;
    }

    private SSGamePackage requireSingleSSGamePackage(
            Map<String, List<SSGamePackage>> ssGamePackagesBySha1,
            FileContext fileContext,
            String area,
            String noIntroTitle
    ) {
        var sha1 = fileContext.getSha1();
        if (sha1 == null || sha1.isBlank()) {
            throw new RuntimeException("FileContext 缺少 SHA1: area=%s, rom=%s, file=%s"
                    .formatted(area, noIntroTitle, fileContext.getFileName()));
        }

        var candidates = ssGamePackagesBySha1.get(normalizeSha1(sha1));
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("SSGamePackage not found: area=%s, rom=%s, file=%s, sha1=%s"
                    .formatted(area, noIntroTitle, fileContext.getFileName(), sha1));
        }
        if (candidates.size() > 1) {
            throw new RuntimeException("Multiple SSGamePackages found: area=%s, rom=%s, file=%s, sha1=%s, ssPackageIds=%s"
                    .formatted(area, noIntroTitle, fileContext.getFileName(), sha1, candidates.stream().map(SSGamePackage::getId).toList()));
        }
        return candidates.get(0);
    }

    private String normalizeSha1(String sha1) {
        return sha1.trim().toUpperCase(Locale.ROOT);
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
        pkg.getNoIntroGameByArea().forEach((area, gameDB) -> names.put(area, gameDB.getName()));
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

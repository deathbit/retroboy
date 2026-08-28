package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.GameDBPackage;
import com.github.deathbit.retroboy.domain.MatchPair;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.match.MatchStrategy;
import com.github.deathbit.retroboy.match.strategy.FullExactMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.FuzzyRatioMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.NoSpaceMatchStrategy;
import com.github.deathbit.retroboy.match.strategy.PartialExactMatchStrategy;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.MatchNameUtils;
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
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class RuleEngineHandler {

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    private static final Pattern REGPARENT_AREA_PATTERN = Pattern.compile("\\(\\s*([A-Z0-9]+)\\s+PARENT\\s*\\)");
    private static final List<String> BASE_AREAS = List.of("USA", "JPN", "EUR");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /** 漏斗策略链（前三层），由紧到松依次执行 */
    private static final List<MatchStrategy> STRATEGIES = List.of(
            new FullExactMatchStrategy(),
            new PartialExactMatchStrategy(),
            new NoSpaceMatchStrategy()
    );

    public void handle(RuleContext ruleContext) throws Exception {
        var processor = platformProcessorMap.get(ruleContext.getPlatform());
        if (processor != null) {
            processor.processGameDB(ruleContext.getGameDBMapByRomName());
        }
        List<GameDB> gameDBS = ruleContext.getGameDBs().stream()
                                          .filter(gameDB -> gameDB.getLicensed().isEmpty())
                                          .filter(gameDB -> gameDB.getBios().isEmpty())
                                          .filter(gameDB -> gameDB.getDevstatus().isEmpty())
                                          .filter(gameDB -> gameDB.getPhysical().isEmpty())
                                          .filter(gameDB -> gameDB.getRegparent().contains("PARENT"))
                                          .toList();

        var gameDBsByArea = new LinkedHashMap<String, List<GameDB>>();
        for (var gameDB : gameDBS) {
            for (var area : extractRegparentAreas(gameDB.getRegparent())) {
                if (shouldAddToArea(ruleContext, area, gameDB)) {
                    gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB);
                }
            }
        }

        ruleContext.setAreas(new ArrayList<>(gameDBsByArea.keySet()));
        ruleContext.setGameDBsByArea(gameDBsByArea);

        // Build game DB packages: group GameDB entries by their root node (clone == "P")
        var rootToAreaGameDB = new LinkedHashMap<String, Map<String, GameDB>>();
        for (var entry : gameDBsByArea.entrySet()) {
            var area = entry.getKey();
            for (var gameDB : entry.getValue()) {
                var rootNumber = "P".equals(gameDB.getClone()) ? gameDB.getNumber() : gameDB.getClone();
                rootToAreaGameDB.computeIfAbsent(rootNumber, ignored -> new LinkedHashMap<>())
                                .put(area, gameDB);
            }
        }

        var gameDBPackages = new ArrayList<GameDBPackage>();
        for (var entry : rootToAreaGameDB.entrySet()) {
            var matchNameByArea = new LinkedHashMap<String, String>();
            entry.getValue().forEach((area, gameDB) ->
                    matchNameByArea.put(area, MatchNameUtils.toMatchName(gameDB.getName())));
            gameDBPackages.add(GameDBPackage.builder()
                                           .id(entry.getKey())
                                           .gameDBByArea(entry.getValue())
                                           .matchNameByArea(matchNameByArea)
                                           .build());
        }
        ruleContext.setGameDBPackages(gameDBPackages);

        if (processor != null) {
            try {
                ruleContext.setWikiDBPackages(processor.processWiki());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ruleContext.setWikiAreaMapping(processor.resolveAreaMapping(ruleContext.getAreas()));
            ruleContext.setMatchResult(buildMatchResult(ruleContext));
        }

        if (ruleContext.getMatchResult() != null) {
            generateMatchReport(ruleContext);
            var matchResult = ruleContext.getMatchResult();
            if (!matchResult.getMismatchWikiDBPackages().isEmpty()) {
                throw new RuntimeException("mismatchWikiDBPackages is not empty: " + matchResult.getMismatchWikiDBPackages().size() + " unmatched wiki packages");
            }
            if (!matchResult.getUnusedGameDBPackages().isEmpty()) {
                throw new RuntimeException("unusedGameDBPackages is not empty: " + matchResult.getUnusedGameDBPackages().size() + " unused game packages");
            }
        }

        System.out.println();
    }

    // ------------------------------------------------------------------ 匹配流程

    private MatchResult buildMatchResult(RuleContext ruleContext) {
        var wikiList = new ArrayList<>(ruleContext.getWikiDBPackages());
        var gameList = new ArrayList<>(ruleContext.getGameDBPackages());
        var areaMapping = ruleContext.getWikiAreaMapping();

        var allPairs = new ArrayList<MatchPair>();

        // 前三层漏斗
        for (var strategy : STRATEGIES) {
            allPairs.addAll(strategy.match(wikiList, gameList, areaMapping));
        }

        // 第四层：WeightedRatio 模糊匹配（跳过 noFuzzyRatioMatch 中指定的 wiki 包）
        var noFuzzy = ruleContext.getPlatformPackTaskConfig().getNoFuzzyRatioMatch();
        var skippedWiki = new ArrayList<WikiDBPackage>();
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
        var mappingList = ruleContext.getPlatformPackTaskConfig().getPackageMappingList();
        if (mappingList != null && !mappingList.isEmpty()) {
            allPairs.addAll(applyDirectMapping(mappingList, wikiList, gameList));
        }

        // 按 MatchLevel 分类（保证所有 level 均有 key）
        var matchPairsByLevel = new EnumMap<MatchLevel, List<MatchPair>>(MatchLevel.class);
        for (var level : MatchLevel.values()) {
            matchPairsByLevel.put(level, new ArrayList<>());
        }
        allPairs.forEach(pair -> matchPairsByLevel.get(pair.getMatchLevel()).add(pair));

        return MatchResult.builder()
                          .matchPairsByLevel(matchPairsByLevel)
                          .mismatchWikiDBPackages(wikiList)
                          .unusedGameDBPackages(gameList)
                          .fuzzyMatchDetails(fuzzyStrategy.getFuzzyMatchDetails())
                          .build();
    }

    // ------------------------------------------------------------------ 报告生成

    private void generateMatchReport(RuleContext ruleContext) {
        var matchResult = ruleContext.getMatchResult();
        var platform = ruleContext.getPlatform().name().toLowerCase();

        var report = new LinkedHashMap<String, Object>();
        for (var level : MatchLevel.values()) {
            var pairs = matchResult.getMatchPairsByLevel().getOrDefault(level, List.of());
            report.put(level.name().toLowerCase(), buildPairReport(pairs));
        }
        report.put("mismatchWikiDBPackages", buildWikiPackageReport(matchResult.getMismatchWikiDBPackages()));
        report.put("unusedGameDBPackages", buildGamePackageReport(matchResult.getUnusedGameDBPackages()));

        var outputPath = Path.of("src/main/resources/platform/" + platform + "/" + platform + "_wiki_db.json");
        try {
            Files.writeString(outputPath, GSON.toJson(report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> buildPairReport(List<MatchPair> pairs) {
        return pairs.stream().map(pair -> {
            var entry = new LinkedHashMap<String, Object>();
            entry.put("wiki", extractWikiNames(pair.getWikiDBPackage()));
            entry.put("game", extractGameNames(pair.getGameDBPackage()));
            return (Map<String, Object>) entry;
        }).toList();
    }

    private List<Map<String, String>> buildWikiPackageReport(List<WikiDBPackage> packages) {
        if (packages == null) return List.of();
        return packages.stream().map(pkg -> {
            var entry = new LinkedHashMap<String, String>();
            entry.put("id", pkg.getId());
            pkg.getWikiDBByArea().forEach((area, wikiDB) -> entry.put(area, wikiDB.getName()));
            return (Map<String, String>) entry;
        }).toList();
    }

    private List<Map<String, String>> buildGamePackageReport(List<GameDBPackage> packages) {
        if (packages == null) return List.of();
        return packages.stream().map(pkg -> {
            var entry = new LinkedHashMap<String, String>();
            entry.put("id", pkg.getId());
            pkg.getGameDBByArea().forEach((area, gameDB) -> entry.put(area, area + " - " + gameDB.getRomName()));
            return (Map<String, String>) entry;
        }).toList();
    }

    private Map<String, String> extractWikiNames(WikiDBPackage pkg) {
        var names = new LinkedHashMap<String, String>();
        pkg.getWikiDBByArea().forEach((area, wikiDB) -> names.put(area, wikiDB.getName()));
        return names;
    }

    private Map<String, String> extractGameNames(GameDBPackage pkg) {
        var names = new LinkedHashMap<String, String>();
        pkg.getGameDBByArea().forEach((area, gameDB) -> names.put(area, gameDB.getName()));
        return names;
    }

    // ------------------------------------------------------------------ 直接映射

    /**
     * 按配置的 "wikiId -> gameId" 列表做直接映射。
     * 条件：两端 ID 均存在于剩余列表中（即未被前几层消耗），满足则配对并从列表移除。
     */
    private List<MatchPair> applyDirectMapping(List<String> mappingList,
                                               List<WikiDBPackage> wikiList,
                                               List<GameDBPackage> gameList) {
        // 校验配置层重复：同一 wiki ID 或 game ID 不得出现多次
        var seenWikiIds = new LinkedHashSet<String>();
        var seenGameIds = new LinkedHashSet<String>();
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            if (parts.length != 2) throw new RuntimeException("packageMappingList 格式错误: " + mapping);
            var wikiId = parts[0].trim();
            var gameId = parts[1].trim();
            if (!seenWikiIds.add(wikiId)) throw new RuntimeException("packageMappingList 中 wiki ID 重复: " + wikiId);
            if (!seenGameIds.add(gameId)) throw new RuntimeException("packageMappingList 中 game ID 重复: " + gameId);
        }

        var pairs = new ArrayList<MatchPair>();
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            var wikiId = parts[0].trim();
            var gameId = parts[1].trim();

            WikiDBPackage wikiPkg = null;
            for (var pkg : wikiList) {
                if (wikiId.equals(pkg.getId())) { wikiPkg = pkg; break; }
            }
            GameDBPackage gamePkg = null;
            for (var pkg : gameList) {
                if (gameId.equals(pkg.getId())) { gamePkg = pkg; break; }
            }
            // 运行层校验：ID 必须存在且未被前几层消耗
            if (wikiPkg == null) throw new RuntimeException("packageMappingList: wiki ID 不存在或已被使用: " + wikiId);
            if (gamePkg == null) throw new RuntimeException("packageMappingList: game ID 不存在或已被使用: " + gameId);

            pairs.add(MatchPair.builder()
                               .wikiDBPackage(wikiPkg)
                               .gameDBPackage(gamePkg)
                               .matchLevel(MatchLevel.DIRECT_MAPPING)
                               .build());
            wikiList.remove(wikiPkg);
            gameList.remove(gamePkg);
        }
        return pairs;
    }

    // ------------------------------------------------------------------ 工具方法

    private List<String> extractRegparentAreas(String regparent) {
        var areas = new LinkedHashSet<String>();
        var matcher = REGPARENT_AREA_PATTERN.matcher(regparent);
        while (matcher.find()) {
            areas.add(matcher.group(1));
        }
        return new ArrayList<>(areas);
    }

    private boolean shouldAddToArea(RuleContext ruleContext, String area, GameDB gameDB) {
        var config = ruleContext.getPlatformPackTaskConfig();
        if (isAreaGameListed(config.getAreaGameBlackList(), area, gameDB)) return false;
        if (isAreaGameListed(config.getAreaGameWhiteList(), area, gameDB)) return true;
        return BASE_AREAS.contains(area) || "P".equals(gameDB.getClone());
    }

    private boolean isAreaGameListed(Set<String> list, String area, GameDB gameDB) {
        if (list == null || list.isEmpty()) return false;
        return list.contains(area + " - " + gameDB.getRomName());
    }
}

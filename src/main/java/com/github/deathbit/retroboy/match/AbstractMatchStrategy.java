package com.github.deathbit.retroboy.match;

import com.github.deathbit.retroboy.domain.GameDBPackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.WikiDBPackage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 匹配策略基类。
 * <p>
 * 策略链的共同前提（需求4）：wiki 包与 game 包的地区集合必须完全一致，
 * 不一致时直接跳过该 game 包。
 * <p>
 * matchNameByArea 由各 Package 在构建时通过 MatchNameUtils.toMatchName 预计算，
 * 策略类只需在此基础上施加额外变换（如去空格），无需重复规范化原始名称。
 */
public abstract class AbstractMatchStrategy implements MatchStrategy {

    // ------------------------------------------------------------------ 可覆盖的变换

    /**
     * 对预计算的 matchName 施加额外变换，默认为恒等（不做任何处理）。
     * NO_SPACE 策略覆盖此方法以去除空格。
     */
    protected String transform(String matchName) {
        return matchName;
    }

    // ------------------------------------------------------------------ 区域集合检查

    /**
     * 判断 wiki 包与 game 包（映射后）的地区集合是否完全相同（需求4）。
     */
    protected boolean hasSameAreaSet(Map<String, String> wikiNames, Map<String, String> gameNames) {
        return wikiNames.keySet().equals(gameNames.keySet());
    }

    // ------------------------------------------------------------------ matchName 获取

    /** 获取 wiki 包各地区经 transform 处理后的 matchName */
    protected Map<String, String> getWikiNames(WikiDBPackage pkg) {
        var result = new LinkedHashMap<String, String>();
        pkg.getMatchNameByArea().forEach((area, name) -> result.put(area, transform(name)));
        return result;
    }

    /**
     * 获取 game 包各地区经地区映射 + transform 处理后的 matchName（key 为 wiki 地区）。
     * 同一 wiki 地区若有多个 game 地区映射，仅保留第一个（putIfAbsent）。
     */
    protected Map<String, String> getGameNames(GameDBPackage pkg, Map<String, String> areaMapping) {
        var result = new LinkedHashMap<String, String>();
        for (var entry : pkg.getMatchNameByArea().entrySet()) {
            var wikiArea = areaMapping.getOrDefault(entry.getKey(), entry.getKey());
            result.putIfAbsent(wikiArea, transform(entry.getValue()));
        }
        return result;
    }

    // ------------------------------------------------------------------ 计分与断言

    /** 判断两个已变换的 matchName 是否匹配，默认为非空精确相等 */
    protected boolean matches(String wikiName, String gameName) {
        return !wikiName.isEmpty() && wikiName.equals(gameName);
    }

    /** 计算 wiki 与 game 地区名称的匹配数量（得分 = 匹配的 area 条数） */
    protected int scoreMatch(Map<String, String> wikiNames, Map<String, String> gameNames) {
        int count = 0;
        for (var entry : wikiNames.entrySet()) {
            var gameName = gameNames.get(entry.getKey());
            if (gameName != null && matches(entry.getValue(), gameName)) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------ 匹配框架

    /**
     * 全匹配：wiki 与 game 的 matchNameByArea（经变换后）完全相等。
     * 地区集合检查由 Map 等值比较隐式完成。
     */
    protected List<MatchPairForPackage> fullExactMatch(List<WikiDBPackage> wikiPackages,
                                                       List<GameDBPackage> gamePackages,
                                                       Map<String, String> areaMapping) {
        var wikiNamesList = wikiPackages.stream().map(this::getWikiNames).toList();
        var gameNamesList = gamePackages.stream().map(p -> getGameNames(p, areaMapping)).toList();

        var usedWiki = new boolean[wikiPackages.size()];
        var usedGame = new boolean[gamePackages.size()];
        var pairs = new ArrayList<MatchPairForPackage>();

        for (int w = 0; w < wikiPackages.size(); w++) {
            for (int g = 0; g < gamePackages.size(); g++) {
                if (usedGame[g]) continue;
                if (wikiNamesList.get(w).equals(gameNamesList.get(g))) {
                    pairs.add(buildPair(wikiPackages.get(w), gamePackages.get(g)));
                    usedWiki[w] = true;
                    usedGame[g] = true;
                    break;
                }
            }
        }

        removeUsed(wikiPackages, usedWiki);
        removeUsed(gamePackages, usedGame);
        return pairs;
    }

    /**
     * 贪心匹配：地区集合必须相同（需求4），为每个 wiki 包选得分最高的 game 包（得分 ≥ 1）。
     * 需求5：PARTIAL_EXACT / NO_SPACE 只要 1 个地区匹配即视为整个 Package 匹配。
     */
    protected List<MatchPairForPackage> greedyMatch(List<WikiDBPackage> wikiPackages,
                                                    List<GameDBPackage> gamePackages,
                                                    Map<String, String> areaMapping) {
        var wikiNamesList = wikiPackages.stream().map(this::getWikiNames).toList();
        var gameNamesList = gamePackages.stream().map(p -> getGameNames(p, areaMapping)).toList();

        var usedWiki = new boolean[wikiPackages.size()];
        var usedGame = new boolean[gamePackages.size()];
        var pairs = new ArrayList<MatchPairForPackage>();

        for (int w = 0; w < wikiPackages.size(); w++) {
            var wikiNames = wikiNamesList.get(w);
            int bestScore = 0;
            int bestG = -1;
            for (int g = 0; g < gamePackages.size(); g++) {
                if (usedGame[g]) continue;
                var gameNames = gameNamesList.get(g);
                // 需求4：地区集合不一致，直接跳过
                if (!hasSameAreaSet(wikiNames, gameNames)) continue;
                int s = scoreMatch(wikiNames, gameNames);
                if (s > bestScore) {
                    bestScore = s;
                    bestG = g;
                }
            }
            if (bestG >= 0) {
                pairs.add(buildPair(wikiPackages.get(w), gamePackages.get(bestG)));
                usedWiki[w] = true;
                usedGame[bestG] = true;
            }
        }

        removeUsed(wikiPackages, usedWiki);
        removeUsed(gamePackages, usedGame);
        return pairs;
    }

    // ------------------------------------------------------------------ 工具

    protected MatchPairForPackage buildPair(WikiDBPackage wiki, GameDBPackage game) {
        return MatchPairForPackage.builder()
                        .wikiDBPackage(wiki)
                        .gameDBPackage(game)
                        .matchLevel(level())
                        .build();
    }

    private static void removeUsed(List<?> list, boolean[] used) {
        for (int i = used.length - 1; i >= 0; i--) {
            if (used[i]) list.remove(i);
        }
    }
}

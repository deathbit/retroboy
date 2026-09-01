package com.github.deathbit.retroboy.match.strategy;

import com.github.deathbit.retroboy.domain.FuzzyAreaResult;
import com.github.deathbit.retroboy.domain.FuzzyCandidate;
import com.github.deathbit.retroboy.domain.FuzzyMatchDetail;
import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.match.AbstractMatchStrategy;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 第 4 层：WeightedRatio 模糊匹配（需求6）。
 * <p>
 * 前提（需求4）：wiki 包与 game 包的地区集合必须完全相同。
 * <p>
 * 匹配逻辑：
 * <ol>
 *   <li>对每个 wiki area，分别与所有候选 game 包中同 area 的 matchName 计算 WeightedRatio。</li>
 *   <li>各 area 取得分最高的 TOP1（需 ≥ 90 分），记录 TOP5 供审计。</li>
 *   <li>收集所有得分 ≥ 90 的 TOP1 候选：若它们全部指向同一个 GameDBPackage，则认为匹配成功。</li>
 *   <li>已匹配的包从候选池移除，不再参与后续计算。</li>
 * </ol>
 * <p>
 * 无论是否匹配成功，每个 wiki 包的中间计算过程均记录在 {@link FuzzyMatchDetail} 中，
 * 可通过 {@link #getFuzzyMatchDetails()} 获取。
 */
public class FuzzyRatioMatchStrategy extends AbstractMatchStrategy {

    private static final int SCORE_THRESHOLD = 90;
    private static final int TOP_N = 5;

    private final List<FuzzyMatchDetail> fuzzyMatchDetails = new ArrayList<>();

    public List<FuzzyMatchDetail> getFuzzyMatchDetails() {
        return fuzzyMatchDetails;
    }

    @Override
    public MatchLevel level() {
        return MatchLevel.FUZZY_RATIO;
    }

    @Override
    public List<MatchPairForPackage> match(List<WikiGamePackage> wikiPackages,
                                           List<NoIntroGamePackage> gamePackages,
                                           Map<String, String> areaMapping) {
        fuzzyMatchDetails.clear();

        var usedGame = new boolean[gamePackages.size()];
        var pairs = new ArrayList<MatchPairForPackage>();
        var toRemoveWiki = new boolean[wikiPackages.size()];

        for (int w = 0; w < wikiPackages.size(); w++) {
            var wikiPkg = wikiPackages.get(w);
            var wikiNames = getWikiNames(wikiPkg);  // wiki area → matchName

            // 为每个 area 计算候选得分
            var areaResults = new LinkedHashMap<String, FuzzyAreaResult>();
            for (var wikiArea : wikiNames.keySet()) {
                var wikiMatchName = wikiNames.get(wikiArea);
                var candidates = buildCandidates(wikiMatchName, wikiArea, wikiNames, gamePackages, usedGame, areaMapping);
                areaResults.put(wikiArea, buildAreaResult(candidates));
            }

            // 收集各 area 中得分 ≥ 阈值的 TOP1 候选，判断是否指向同一个 GameDBPackage
            NoIntroGamePackage matched = resolveMatch(areaResults, wikiNames.keySet());

            int matchedIdx = -1;
            if (matched != null) {
                matchedIdx = gamePackages.indexOf(matched);
            }

            fuzzyMatchDetails.add(FuzzyMatchDetail.builder()
                    .wikiGamePackage(wikiPkg)
                    .areaResults(areaResults)
                    .matchedNoIntroGamePackage(matched)
                    .build());

            if (matched != null && matchedIdx >= 0) {
                pairs.add(buildPair(wikiPkg, matched));
                toRemoveWiki[w] = true;
                usedGame[matchedIdx] = true;
            }
        }

        removeUsed(wikiPackages, toRemoveWiki);
        // game 包通过 usedGame 标记，但需要从列表中实际移除
        removeUsedGame(gamePackages, usedGame);
        return pairs;
    }

    // ------------------------------------------------------------------ 候选计算

    /**
     * 针对某个 wiki area 的 matchName，遍历所有未使用的 game 包，
     * 找出对应 wiki area 的 game matchName，计算 WeightedRatio，返回所有候选。
     * <p>
     * 需求4：game 包的地区集合必须与 wiki 包完全一致，否则跳过。
     */
    private List<FuzzyCandidate> buildCandidates(String wikiMatchName,
                                                   String wikiArea,
                                                   Map<String, String> wikiNames,
                                                   List<NoIntroGamePackage> gamePackages,
                                                   boolean[] usedGame,
                                                   Map<String, String> areaMapping) {
        var candidates = new ArrayList<FuzzyCandidate>();
        for (int g = 0; g < gamePackages.size(); g++) {
            if (usedGame[g]) continue;
            var gamePkg = gamePackages.get(g);
            var gameMatchNames = getGameNames(gamePkg, areaMapping);

            // 需求4：地区集合必须完全相同，数量或内容不一致均跳过
            if (!hasSameAreaSet(wikiNames, gameMatchNames)) continue;

            var gameMatchName = gameMatchNames.get(wikiArea);
            if (gameMatchName == null) continue;

            int score = FuzzySearch.weightedRatio(wikiMatchName, gameMatchName);
            candidates.add(FuzzyCandidate.builder()
                    .noIntroGamePackage(gamePkg)
                    .wikiMatchName(wikiMatchName)
                    .gameMatchName(gameMatchName)
                    .score(score)
                    .build());
        }
        candidates.sort(Comparator.comparingInt(FuzzyCandidate::getScore).reversed());
        return candidates;
    }

    private FuzzyAreaResult buildAreaResult(List<FuzzyCandidate> candidates) {
        var top5 = candidates.stream().limit(TOP_N).toList();
        FuzzyCandidate selected = null;
        if (!top5.isEmpty() && top5.get(0).getScore() >= SCORE_THRESHOLD) {
            selected = top5.get(0);
        }
        return FuzzyAreaResult.builder()
                .totalCandidates(candidates.size())
                .top5(top5)
                .selected(selected)
                .build();
    }

    // ------------------------------------------------------------------ 共识判定

    /**
     * 判断各 area 的 selected 候选是否唯一收敛到同一个 GameDBPackage。
     * 条件：至少有一个 area 有 selected，且所有 selected 指向同一个 game 包。
     */
    private NoIntroGamePackage resolveMatch(Map<String, FuzzyAreaResult> areaResults,
                                        Set<String> wikiAreas) {
        // 收集所有 area 的 selected（需满足 area 集合相同——只有 area 存在于 gameNames 才会有候选）
        Set<NoIntroGamePackage> selectedGames = areaResults.values().stream()
                                                           .map(FuzzyAreaResult::getSelected)
                                                           .filter(Objects::nonNull)
                                                           .map(FuzzyCandidate::getNoIntroGamePackage)
                                                           .collect(Collectors.toSet());

        // 地区集合已在 buildCandidates 中保证一致，这里只需确认所有 area 均有候选且收敛到同一个包
        if (selectedGames.size() == 1) {
            long coveredAreas = areaResults.values().stream()
                    .filter(r -> r.getTotalCandidates() > 0)
                    .count();
            if (coveredAreas == wikiAreas.size()) {
                return selectedGames.iterator().next();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ 工具

    private static void removeUsed(List<?> list, boolean[] used) {
        for (int i = used.length - 1; i >= 0; i--) {
            if (used[i]) list.remove(i);
        }
    }

    private static void removeUsedGame(List<NoIntroGamePackage> list, boolean[] usedGame) {
        for (int i = usedGame.length - 1; i >= 0; i--) {
            if (usedGame[i]) list.remove(i);
        }
    }
}

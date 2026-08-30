package com.github.deathbit.retroboy.match.strategy;

import com.github.deathbit.retroboy.domain.GameDBPackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.match.AbstractMatchStrategy;

import java.util.List;
import java.util.Map;

/**
 * 第 2 层：部分区域完全匹配（贪心）。
 * 前提：wiki 与 game 地区集合必须完全相同（需求4）。
 * 只要有至少一个 area 的 matchName 相等即视为整个 Package 匹配（需求5）。
 * 贪心策略：为每个 wiki 包选取匹配 area 数最多的 game 包。
 */
public class PartialExactMatchStrategy extends AbstractMatchStrategy {

    @Override
    public MatchLevel level() {
        return MatchLevel.PARTIAL_EXACT;
    }

    @Override
    public List<MatchPairForPackage> match(List<WikiDBPackage> wikiPackages,
                                           List<GameDBPackage> gamePackages,
                                           Map<String, String> areaMapping) {
        return greedyMatch(wikiPackages, gamePackages, areaMapping);
    }
}

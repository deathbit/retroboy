package com.github.deathbit.retroboy.match.strategy;

import com.github.deathbit.retroboy.domain.GameDBPackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.match.AbstractMatchStrategy;

import java.util.List;
import java.util.Map;

/**
 * 第 1 层：全区域完全匹配。
 * 要求 wiki 包与 game 包的 matchNameByArea 完全一致：key 集合相同且每个 area 对应的 matchName 相等。
 */
public class FullExactMatchStrategy extends AbstractMatchStrategy {

    @Override
    public MatchLevel level() {
        return MatchLevel.FULL_EXACT;
    }

    @Override
    public List<MatchPairForPackage> match(List<WikiDBPackage> wikiPackages,
                                           List<GameDBPackage> gamePackages,
                                           Map<String, String> areaMapping) {
        return fullExactMatch(wikiPackages, gamePackages, areaMapping);
    }
}

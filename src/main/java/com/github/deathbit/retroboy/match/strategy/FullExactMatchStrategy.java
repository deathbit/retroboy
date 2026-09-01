package com.github.deathbit.retroboy.match.strategy;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.match.AbstractMatchStrategy;

import java.util.List;
import java.util.Map;

/**
 * 第 1 层：全区域完全匹配。
 * 要求 wiki 包与 No-Intro 包动态计算出的 matchName 完全一致：key 集合相同且每个 area 对应的 matchName 相等。
 */
public class FullExactMatchStrategy extends AbstractMatchStrategy {

    @Override
    public MatchLevel level() {
        return MatchLevel.FULL_EXACT;
    }

    @Override
    public List<MatchPairForPackage> match(List<WikiGamePackage> wikiPackages,
        List<NoIntroGamePackage> gamePackages,
        Map<String, String> areaMapping
    ) {
        return fullExactMatch(wikiPackages, gamePackages, areaMapping);
    }
}

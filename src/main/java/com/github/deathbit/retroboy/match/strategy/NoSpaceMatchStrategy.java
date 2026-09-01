package com.github.deathbit.retroboy.match.strategy;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import com.github.deathbit.retroboy.match.AbstractMatchStrategy;

import java.util.List;
import java.util.Map;

/**
 * 第 3 层：去除所有空格后匹配（贪心）。
 * 前提：wiki 与 game 地区集合必须完全相同（需求4）。
 * 在预计算 matchName 基础上去除全部空格后，至少一个 area 完全匹配即视为 Package 匹配（需求5）。
 * 适用于 DuckTales vs Duck Tales 这类空格位置不一致的情况。
 */
public class NoSpaceMatchStrategy extends AbstractMatchStrategy {

    @Override
    public MatchLevel level() {
        return MatchLevel.NO_SPACE;
    }

    @Override
    public List<MatchPairForPackage> match(List<WikiGamePackage> wikiPackages,
                                           List<NoIntroGamePackage> gamePackages,
                                           Map<String, String> areaMapping) {
        return greedyMatch(wikiPackages, gamePackages, areaMapping);
    }

    @Override
    protected String transform(String matchName) {
        return matchName.replace(" ", "");
    }
}

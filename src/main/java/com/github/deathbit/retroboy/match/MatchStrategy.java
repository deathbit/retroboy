package com.github.deathbit.retroboy.match;

import com.github.deathbit.retroboy.domain.GameDBPackage;
import com.github.deathbit.retroboy.domain.MatchPairForPackage;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.MatchLevel;

import java.util.List;
import java.util.Map;

/**
 * 匹配策略接口。
 * 每个实现类代表漏斗中的一层，负责对传入的剩余未匹配包执行本层匹配逻辑，
 * 并将已匹配的包从 wikiPackages / gamePackages 中移除，返回本层产生的匹配对。
 */
public interface MatchStrategy {

    MatchLevel level();

    /**
     * @param wikiPackages  剩余未匹配的 WikiDB 包（可变列表，匹配成功的条目会被移除）
     * @param gamePackages  剩余未匹配的 GameDB 包（可变列表，匹配成功的条目会被移除）
     * @param wikiAreaMapping GameDB 地区 → WikiDB 地区的映射
     * @return 本层产生的匹配对列表
     */
    List<MatchPairForPackage> match(List<WikiDBPackage> wikiPackages,
                                    List<GameDBPackage> gamePackages,
                                    Map<String, String> wikiAreaMapping);
}

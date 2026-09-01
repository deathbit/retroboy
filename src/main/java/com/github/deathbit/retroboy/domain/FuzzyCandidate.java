package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WeightedRatio 匹配中，某个地区内单个候选 GameDBPackage 的得分详情。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuzzyCandidate {
    private NoIntroGamePackage noIntroGamePackage;
    /** 该地区 WikiDBPackage 的 matchName */
    private String wikiMatchName;
    /** 该地区 GameDBPackage 的 matchName */
    private String gameMatchName;
    /** WeightedRatio 得分（0-100） */
    private int score;
}

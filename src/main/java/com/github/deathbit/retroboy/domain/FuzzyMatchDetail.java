package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * WeightedRatio 匹配中，单个 WikiDBPackage 的完整中间计算过程。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuzzyMatchDetail {
    private WikiGamePackage wikiGamePackage;
    /** wiki 地区 → 该地区的计算结果 */
    private Map<String, FuzzyAreaResult> areaResults;
    /**
     * 最终匹配到的 GameDBPackage（各地区 TOP1 唯一且均指向同一个时赋值），
     * null 表示未匹配。
     */
    private NoIntroGamePackage matchedNoIntroGamePackage;
}

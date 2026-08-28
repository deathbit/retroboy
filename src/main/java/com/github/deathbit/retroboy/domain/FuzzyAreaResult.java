package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WeightedRatio 匹配中，某个 WikiDBPackage 在单个地区的中间计算结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuzzyAreaResult {
    /** 该地区参与计算的候选 GameDBPackage 总数 */
    private int totalCandidates;
    /** 得分最高的 TOP5 候选（已按得分降序排列） */
    private List<FuzzyCandidate> top5;
    /**
     * 最终选用的候选（即 TOP1 且得分 ≥ 90 时），null 表示该地区无满足阈值的候选。
     */
    private FuzzyCandidate selected;
}

package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.MatchLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    /** 按 MatchLevel 分类的匹配对，每个 level 均有 key（可能为空 list） */
    private Map<MatchLevel, List<MatchPairForPackage>> matchPairsByLevel;
    /** 未能匹配到任何 GameDBPackage 的 WikiDBPackage */
    private List<WikiDBPackage> mismatchWikiDBPackages;
    /** 未被任何 WikiDBPackage 匹配的 GameDBPackage */
    private List<GameDBPackage> unusedGameDBPackages;
    /** FUZZY_RATIO 阶段每个 WikiDBPackage 的中间计算过程（无论是否匹配成功） */
    private List<FuzzyMatchDetail> fuzzyMatchDetails;
}

package com.github.deathbit.retroboy.enums;

public enum MatchLevel {
    /** 全区域名称完全匹配（key 集合与每个 area 的 matchName 全部一致） */
    FULL_EXACT,
    /** 区域集合相同，至少有一个 area 的 matchName 完全匹配 */
    PARTIAL_EXACT,
    /** 区域集合相同，去除所有空格后至少一个 area 的 matchName 完全匹配 */
    NO_SPACE,
    /** 区域集合相同，WeightedRatio 得分 ≥ 90 且各 area TOP1 指向同一个 GameDBPackage */
    FUZZY_RATIO,
    /** 配置文件中显式指定的 wiki 包 ID → game 包 ID 直接映射（最后兜底） */
    DIRECT_MAPPING
}

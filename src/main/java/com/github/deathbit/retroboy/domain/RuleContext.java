package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    private Platform platform;
    private RuleConfig ruleConfig;
    private Set<String> licensed;
    private Set<String> globalTagBlackList;
    private Map<String, FileContext> fileContextMap;
    private Map<Area, Set<String>> areaFinalMap;
}

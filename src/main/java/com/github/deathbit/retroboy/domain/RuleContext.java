package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.config.GlobalConfig;
import com.github.deathbit.retroboy.config.PlatformPackTaskConfig;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    private Platform platform;
    private String platformName;
    private GlobalConfig globalConfig;
    private PlatformPackTaskConfig platformPackTaskConfig;
    private Map<String, String> renameOptionMap;
    private Set<String> licensed;
    private Map<String, FileContext> fileContextMap;
    private Map<Area, Rule> ruleMap;
    private AreaConfig currentAreaConfig;
    private Map<Area, List<String>> areaPassMap;
    private Map<Area, List<String>> areaNotPassReportMap;
    private List<String> romNotPassReasons;
}

package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.GlobalConfig;
import com.github.deathbit.retroboy.config.PlatformPackTaskConfig;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.enums.Platform;
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
    private AppConfig appConfig;
    private GlobalConfig globalConfig;
    private PlatformPackTaskConfig platformPackTaskConfig;
    private Map<String, String> renameOptionMap;
    private List<GameDB> gameDBs;
    private Map<String, GameDB> gameDBMapByRomName;
    private Map<String, GameDB> gameDBMapByNumber;
    private List<GameDBPackage> gameDBPackages;
    private List<WikiDBPackage> wikiDBPackages;
    private Map<String, String> wikiAreaMapping;
    private MatchResult matchResult;
    private List<String> areas;
    private Map<String, List<GameDB>> gameDBsByArea;
    private Set<String> licensed;
    private Map<String, FileContext> fileContextMap;
    private AreaConfig currentAreaConfig;
    private List<String> romNotPassReasons;
    private Map<Area, List<String>> areaPassMap;
    private Map<Area, Map<String, AreaRuleResult>> areaRuleResultMap;
    private Map<Area, Map<String, AreaRenameResult>> areaRenameResultMap;
    private Map<Area, Map<String, WikiGameEntry>> areaWikiEntryMap;
    private Map<Area, Map<MediaAssetType, MediaCompletionRate>> mediaCompletionRateMap;
}

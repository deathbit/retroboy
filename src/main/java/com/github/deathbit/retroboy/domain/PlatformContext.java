package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.GlobalConfig;
import com.github.deathbit.retroboy.config.PlatformPackTaskConfig;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
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
public class PlatformContext {
    private Platform platform;
    private String platformName;

    private AppConfig appConfig;
    private GlobalConfig globalConfig;
    private PlatformPackTaskConfig platformPackTaskConfig;
    private PlatformProcessor platformProcessor;

    private List<GameDB> gameDBs;
    private Map<String, GameDB> gameDBMapByNumber;
    private Map<String, GameDB> gameDBMapByRomName;
    private Map<String, List<GameDB>> gameDBsByArea;
    private List<String> gameDBAreas;
    private List<GameDBPackage> gameDBPackages;
    private Map<String, GameDBPackage> gameDBPackageById;

    private List<WikiDB> wikiDBs;
    private Map<String, WikiDB> wikiDBById;
    private Map<String, WikiDB> wikiDBByName;
    private List<String> wikiDBAreas;
    private Map<String, List<WikiDB>> wikiDBsByArea;
    private List<WikiDBPackage> wikiDBPackages;
    private Map<String, WikiDBPackage> wikiDBPackageById;

    private Map<String, String> gameDBToWikiDBAreaMapping;
    private MatchResult matchResult;
    private Map<String, List<MatchPairForGame>> matchPairForGamesByArea;

    private Map<String, FileContext> fileContextMap;
    private Map<String, Map<String, String>> renameResultByArea;

    private Map<String, Map<String, FinalGame>> finalGameMapByArea;

    private Map<String, Map<MediaAssetType, MediaCompletionRate>> mediaCompletionRateMap;
}

package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.config.GlobalConfig;
import com.github.deathbit.retroboy.config.PlatformPackTaskConfig;
import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.enums.Platform;
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

    private GlobalConfig globalConfig;
    private PlatformPackTaskConfig platformPackTaskConfig;

    private List<WikiGamePackage> wikiGamePackages;
    private List<NoIntroGamePackage> noIntroGamePackages;
    private List<SSGamePackage> ssGamePackages;
    private List<FileContext> fileContexts;

    private List<MatchResult> matchResults;

    private Map<String, Map<String, String>> renameResultByArea;

    private Map<String, Map<String, FinalGame>> finalGameMapByArea;

    private Map<String, Map<MediaAssetType, MediaCompletionRate>> mediaCompletionRateMap;
}

package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.domain.ReleaseNote;
import com.github.deathbit.retroboy.domain.RenameOption;
import com.github.deathbit.retroboy.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformPackTaskConfig {
    private Platform platform;
    private String version;
    private boolean enabled;
    private boolean manualStep;
    private boolean release;
    private boolean usePal;
    private String core;
    private String wiki;
    private List<ReleaseNote> releaseNotes;
    private List<Path> coreConfigs;
    private Set<String> areaGameBlackList;
    private Set<String> areaGameWhiteList;
    private List<RenameOption> renameOptions;
    /** wiki 包 ID → game 包 ID 的直接映射，格式："wikiId -> gameId" */
    private List<String> packageMappingList;
    /** 跳过 FuzzyRatio 匹配的 wiki 包 ID 集合 */
    private Set<String> noFuzzyRatioMatch;
}

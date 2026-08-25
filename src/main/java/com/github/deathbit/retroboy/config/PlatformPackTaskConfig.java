package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.domain.AreaConfig;
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
    private List<AreaConfig> areaConfigs;
    private Set<String> tagBlackList;
    private Set<String> fileNameBlackList;
    private Set<String> areaGameBlackList;
    private List<RenameOption> renameOptions;
}

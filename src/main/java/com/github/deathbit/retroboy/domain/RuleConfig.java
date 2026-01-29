package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.Platform;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class RuleConfig {
    private Platform platform;
    private String datFile;
    private String romDir;
    private String targetDirBase;
    private List<AreaConfig> targetAreaConfigs;
    private Set<String> tagBlackList;
    private Set<String> fileNameBlackList;
}

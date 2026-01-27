package com.github.deathbit.retroboy.domain;

import java.util.List;

import com.github.deathbit.retroboy.enums.Platform;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleConfig {
    private Platform platform;
    private String datFile;
    private String romDir;
    private String targetDirBase;
    private List<AreaConfig> targetAreaConfigs;
}

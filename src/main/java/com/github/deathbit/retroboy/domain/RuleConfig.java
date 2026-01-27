package com.github.deathbit.retroboy.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleConfig {
    private String platform;
    private String datFile;
    private String romDir;
    private String targetDirBase;
    private List<AreaConfig> targetAreaConfigs;
}

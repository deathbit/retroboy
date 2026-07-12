package com.github.deathbit.retroboy.config;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class GlobalConfig {
    private String raConfig;
    private String downloadedMediaDirBase;
    private String romsAllDir;
    private String romsDir;
    private String startupTaskMask;
    private String platformTaskMask;
    private Set<String> globalTagBlacklist;
    private Set<String> globalRomWhitelist;
}

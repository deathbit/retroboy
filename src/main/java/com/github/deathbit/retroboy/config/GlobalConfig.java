package com.github.deathbit.retroboy.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfig {
    private boolean enableBasePackHandler;
    private boolean enablePlatformPackHandler;
    private String esdeHomePath;
    private String retroarchHomePath;
    private String resourcesHomePath;




    private String raConfig;
    private String downloadedMediaDirBase;
    private String romsAllDir;
    private String romsDir;
    private String startupTaskMask;
    private String platformTaskMask;
    private Set<String> globalTagBlacklist;
    private Set<String> globalRomWhitelist;
}

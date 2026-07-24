package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.domain.AuthorInfo;
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
    private String repo;
    private String baiduPan;
    private String qqGroup;
    private String feedbackEmail;
    private AuthorInfo authorInfo;
    private Set<String> globalTagBlacklist;
    private Set<String> globalRomWhitelist;
}

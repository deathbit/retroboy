package com.github.deathbit.retroboy.config;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class GlobalConfig {
    private String esdeHome;
    private String raHome;
    private String resourcesHome;
    private String raConfig;
    private Set<String> globalTagBlacklist;
}

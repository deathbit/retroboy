package com.github.deathbit.retroboy.config;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalConfig {
    private String raConfigFile;
    private Set<String> tagBlacklist;
}

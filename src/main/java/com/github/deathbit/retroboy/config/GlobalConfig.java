package com.github.deathbit.retroboy.config;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GlobalConfig {
    private String raConfigFile;
    private List<String> tagBlacklist;
}

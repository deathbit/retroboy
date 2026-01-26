package com.github.deathbit.retroboy.config;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalConfig {
    private String raMainConfig;
    private List<String> tagBlacklist;
}

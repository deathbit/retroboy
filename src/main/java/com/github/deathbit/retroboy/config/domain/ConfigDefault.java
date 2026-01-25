package com.github.deathbit.retroboy.config.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfigDefault {
    private List<Config> retroArchConfigs;
}

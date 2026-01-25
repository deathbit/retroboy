package com.github.deathbit.retroboy.config.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Config {
    private String configFile;
    private String key;
    private String value;
}

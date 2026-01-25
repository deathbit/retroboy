package com.github.deathbit.retroboy.config.domain;

import lombok.Data;

@Data
public class Config {
    private String configFile;
    private String key;
    private String value;
}

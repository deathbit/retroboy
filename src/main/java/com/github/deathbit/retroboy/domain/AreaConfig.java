package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaConfig {
    private String name;
    private String targetDir;
}

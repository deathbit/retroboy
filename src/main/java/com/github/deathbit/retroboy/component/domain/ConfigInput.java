package com.github.deathbit.retroboy.component.domain;

import java.nio.file.Path;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigInput {
    private Path file;
    private String key;
    private String value;
}

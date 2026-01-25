package com.github.deathbit.retroboy.config.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyDir {
    private String src;
    private String dest;
}

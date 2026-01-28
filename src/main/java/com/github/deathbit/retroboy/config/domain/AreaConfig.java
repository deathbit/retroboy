package com.github.deathbit.retroboy.config.domain;

import com.github.deathbit.retroboy.enums.Area;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaConfig {
    private Area area;
}

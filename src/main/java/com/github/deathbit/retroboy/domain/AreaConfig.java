package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.Area;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaConfig {
    private Area area;
}

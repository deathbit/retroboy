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
public class WikiGameEntry {
    private Area area;
    private String wikiName;
    private int lineNumber;
}


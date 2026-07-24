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
public class AreaWikiMismatch {
    private Area area;
    private String wikiName;
    private String originalFileName;
    private String renamedFileName;
    private String reason;
    private int wikiLineNumber;
}


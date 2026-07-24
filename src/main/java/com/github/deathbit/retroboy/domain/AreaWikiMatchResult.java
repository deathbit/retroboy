package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.WikiMatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaWikiMatchResult {
    private String wikiName;
    private String originalFileName;
    private String renamedFileName;
    private WikiMatchType matchType;
    private int wikiLineNumber;
}


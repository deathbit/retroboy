package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.MatchLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchPair {
    private MatchLevel matchLevel;
    private WikiDBPackage wikiDBPackage;
    private GameDBPackage gameDBPackage;
}

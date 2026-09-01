package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MatchLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchPairForPackage {
    private MatchLevel matchLevel;
    private WikiGamePackage wikiGamePackage;
    private NoIntroGamePackage noIntroGamePackage;
}

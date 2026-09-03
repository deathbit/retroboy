package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    private WikiGamePackage wikiGamePackage;
    private NoIntroGamePackage noIntroGamePackage;
    private Map<String, FileContext> fileContextByArea;
    private Map<String, SSGamePackage> ssGamePackageByArea;
    private Map<String, Map<String, String>> renameResultByArea;
}

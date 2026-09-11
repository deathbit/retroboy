package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.domain.game.WikiGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalGame {
    private String finalRomName;
    private String originRomName;
    private String wikiName;
    private WikiGame wikiGame;
    private NoIntroGame noIntroGame;
    private FileContext fileContext;
    private int mediaBitMap;
}

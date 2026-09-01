package com.github.deathbit.retroboy.domain.gamepackage;

import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoIntroGamePackage implements GamePackage {
    private String id;
    private Map<String, NoIntroGame> noIntroGameByArea;
}

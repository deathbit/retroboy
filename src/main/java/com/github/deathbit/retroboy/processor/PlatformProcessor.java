package com.github.deathbit.retroboy.processor;

import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.enums.Platform;

import java.util.List;
import java.util.Map;

public interface PlatformProcessor {

    Platform platform();

    void preProcessGameDB(Map<String, NoIntroGame> noIntroGameByTitle);


    Map<String, String> gameDBToWikiDBAreaMapping(List<String> gameDBAreas, List<String> wikiDBAreas);
}

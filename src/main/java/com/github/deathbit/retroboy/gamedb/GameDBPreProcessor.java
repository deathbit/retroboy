package com.github.deathbit.retroboy.gamedb;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.enums.Platform;

import java.util.Map;
import java.util.function.Consumer;

public interface GameDBPreProcessor {

    Platform platform();

    void process(Map<String, GameDB> gameDBMap);

    default void set(Map<String, GameDB> gameDBMap, String romName, Consumer<GameDB> setter) {
        var gameDB = gameDBMap.get(romName);
        if (gameDB != null) {
            setter.accept(gameDB);
        }
    }
}

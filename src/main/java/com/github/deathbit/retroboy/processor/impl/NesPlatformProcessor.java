package com.github.deathbit.retroboy.processor.impl;

import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NesPlatformProcessor implements PlatformProcessor {

    @Override
    public Platform platform() {
        return Platform.NES;
    }

    @Override
    public void preProcessGameDB(Map<String, NoIntroGame> noIntroGameByTitle) {
        noIntroGameByTitle.get("Adventures of Lolo (Japan) (En)").setClone("0061");
        noIntroGameByTitle.get("Adventures of Lolo II (Japan)").setClone("0064");
        noIntroGameByTitle.get("Mario Bros. (World)").setRegparent("(USA PARENT) (JPN PARENT) (EUR PARENT)");
        noIntroGameByTitle.get("Mario Bros. (World)").setClone("P");
        noIntroGameByTitle.get("Mike Tyson's Punch-Out!! (Europe) (Rev 1)").setClone("P");
        noIntroGameByTitle.get("Mike Tyson's Punch-Out!! (Europe) (Rev 1)").setRegparent("(EUR PARENT)"); // 1459
        noIntroGameByTitle.get("Mike Tyson's Punch-Out!! (Japan, USA) (En) (Rev 1)").setClone("1459");
        noIntroGameByTitle.get("Mike Tyson's Punch-Out!! (Japan, USA) (En) (Rev 1)").setRegparent("(USA PARENT) (JPN PARENT)");
        noIntroGameByTitle.get("Power Blazer (Japan)").setClone("1703");
        noIntroGameByTitle.get("Nagagutsu o Haita Neko - Sekai Isshuu 80 Nichi Daibouken (Japan)").setClone("1740");
        noIntroGameByTitle.get("Romancia (Japan)").setClone("P");
        noIntroGameByTitle.get("Romancia (Japan)").setRegparent("(JPN PARENT)");
        noIntroGameByTitle.get("Sky Shark (USA)").setRegparent("(USA PARENT)");
        noIntroGameByTitle.get("Tiger-Heli (USA)").setClone("2720");
        noIntroGameByTitle.get("Tiger-Heli (USA)").setRegparent("(USA PARENT)");
        noIntroGameByTitle.get("Kyuukyoku Harikiri Stadium (Japan)").setClone("P");
        noIntroGameByTitle.get("Kyuukyoku Harikiri Stadium (Japan)").setRegparent("(JPN PARENT)");
    }


    @Override
    public Map<String, String> gameDBToWikiDBAreaMapping(List<String> gameDBAreas, List<String> wikiDBAreas) {
        var mapping = new LinkedHashMap<String, String>();
        for (var gameArea : gameDBAreas) {
            var wikiArea = switch (gameArea) {
                case "JPN" -> "JPN";
                case "USA" -> "USA";
                default -> "PAL";
            };
            mapping.put(gameArea, wikiArea);
        }

        return mapping;
    }
}

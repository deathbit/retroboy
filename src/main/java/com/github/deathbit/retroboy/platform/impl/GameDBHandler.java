package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.GameDBPackage;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.util.MatchNameUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class GameDBHandler {

    private static final Pattern REGPARENT_AREA_PATTERN = Pattern.compile("\\(\\s*([A-Z0-9]+)\\s+PARENT\\s*\\)");
    private static final List<String> BASE_AREAS = List.of("USA", "JPN", "EUR");

    public void handle(PlatformContext platformContext) throws Exception {
        platformContext.getPlatformProcessor().preProcessGameDB(platformContext.getGameDBMapByRomName());

        List<GameDB> gameDBS = platformContext.getGameDBs().stream()
                                              .filter(gameDB -> gameDB.getLicensed().isEmpty())
                                              .filter(gameDB -> gameDB.getBios().isEmpty())
                                              .filter(gameDB -> gameDB.getDevstatus().isEmpty())
                                              .filter(gameDB -> gameDB.getPhysical().isEmpty())
                                              .filter(gameDB -> gameDB.getRegparent().contains("PARENT"))
                                              .toList();

        var gameDBsByArea = new LinkedHashMap<String, List<GameDB>>();
        for (var gameDB : gameDBS) {
            for (var area : extractRegparentAreas(gameDB.getRegparent())) {
                if (shouldAddToArea(platformContext, area, gameDB)) {
                    gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB);
                }
            }
        }

        platformContext.setGameDBsByArea(gameDBsByArea);
        platformContext.setGameDBAreas(new ArrayList<>(gameDBsByArea.keySet()));

        // Build game DB packages: group GameDB entries by their root node (clone == "P")
        var rootToAreaGameDB = new LinkedHashMap<String, Map<String, GameDB>>();
        for (var entry : gameDBsByArea.entrySet()) {
            var area = entry.getKey();
            for (var gameDB : entry.getValue()) {
                var rootNumber = "P".equals(gameDB.getClone()) ? gameDB.getNumber() : gameDB.getClone();
                rootToAreaGameDB.computeIfAbsent(rootNumber, ignored -> new LinkedHashMap<>())
                                .put(area, gameDB);
            }
        }

        var gameDBPackages = new ArrayList<GameDBPackage>();
        for (var entry : rootToAreaGameDB.entrySet()) {
            var matchNameByArea = new LinkedHashMap<String, String>();
            entry.getValue().forEach((area, gameDB) ->
                matchNameByArea.put(area, MatchNameUtils.toMatchName(gameDB.getName())));
            gameDBPackages.add(GameDBPackage.builder()
                                            .id(entry.getKey())
                                            .gameDBByArea(entry.getValue())
                                            .matchNameByArea(matchNameByArea)
                                            .build());
        }
        platformContext.setGameDBPackages(gameDBPackages);
        platformContext.setGameDBPackageById(gameDBPackages.stream().collect(Collectors.toMap(GameDBPackage::getId, Function.identity())));
    }

    private List<String> extractRegparentAreas(String regparent) {
        var areas = new LinkedHashSet<String>();
        var matcher = REGPARENT_AREA_PATTERN.matcher(regparent);
        while (matcher.find()) {
            areas.add(matcher.group(1));
        }
        return new ArrayList<>(areas);
    }

    private boolean shouldAddToArea(PlatformContext platformContext, String area, GameDB gameDB) {
        var config = platformContext.getPlatformPackTaskConfig();
        if (isAreaGameListed(config.getAreaGameBlackList(), area, gameDB)) return false;
        if (isAreaGameListed(config.getAreaGameWhiteList(), area, gameDB)) return true;
        return BASE_AREAS.contains(area) || "P".equals(gameDB.getClone());
    }

    private boolean isAreaGameListed(Set<String> list, String area, GameDB gameDB) {
        if (list == null || list.isEmpty()) return false;
        return list.contains(area + " - " + gameDB.getRomName());
    }
}

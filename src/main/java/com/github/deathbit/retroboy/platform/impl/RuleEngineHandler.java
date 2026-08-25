package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class RuleEngineHandler {
    private static final Pattern REGPARENT_AREA_PATTERN = Pattern.compile("\\(\\s*([A-Z0-9]+)\\s+PARENT\\s*\\)");
    private static final List<String> BASE_AREAS = List.of("USA", "JPN", "EUR");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public void handle(RuleContext ruleContext) {
        // TODO GameDB pre-process
        List<GameDB> gameDBS = ruleContext.getGameDBs().stream()
                                          .filter(gameDB -> gameDB.getLicensed().isEmpty())
                                          .filter(gameDB -> gameDB.getBios().isEmpty())
                                          .filter(gameDB -> gameDB.getDevstatus().isEmpty())
                                          .filter(gameDB -> gameDB.getPhysical().isEmpty())
                                          .filter(gameDB -> gameDB.getRegparent().contains("PARENT"))
                                          .toList();

        var gameDBsByArea = new LinkedHashMap<String, List<GameDB>>();
        for (var gameDB : gameDBS) {
            for (var area : extractRegparentAreas(gameDB.getRegparent())) {
                if (shouldAddToArea(ruleContext, area, gameDB)) {
                    gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB);
                }
            }
        }

        ruleContext.setAreas(new ArrayList<>(gameDBsByArea.keySet()));
        ruleContext.setGameDBsByArea(gameDBsByArea);

        generateGameDbJson(ruleContext);

        System.out.println();

        //        for (var areaConfig : ruleContext.getPlatformPackTaskConfig().getAreaConfigs()) {
        //            var area = areaConfig.getArea();
        //            var rule = ruleContext.getRuleMap().get(area);
        //            ruleContext.setCurrentAreaConfig(areaConfig);
        //            var areaRuleResults = ruleContext.getAreaRuleResultMap().computeIfAbsent(area, ignored -> new LinkedHashMap<>());
        //            var fileContextEntries = ruleContext.getFileContextMap().entrySet().stream().toList();
        //            ProgressBar pb = new ProgressBar("解析规则");
        //            pb.startTask(fileContextEntries.size());
        //            for (int i = 0; i < fileContextEntries.size(); i++) {
        //                var entry = fileContextEntries.get(i);
        //                var fileName = entry.getKey();
        //                var fileContext = entry.getValue();
        //                ruleContext.setRomNotPassReasons(new ArrayList<>());
        //                var passed = rule.pass(ruleContext, fileContext);
        //                var reasons = passed ? new ArrayList<String>() : new ArrayList<>(ruleContext.getRomNotPassReasons());
        //                areaRuleResults.put(fileName, AreaRuleResult.builder()
        //                                                            .fileName(fileName)
        //                                                            .passed(passed)
        //                                                            .reasons(reasons)
        //                                                            .build());
        //                if (passed) {
        //                    ruleContext.getAreaPassMap().computeIfAbsent(area, ignored -> new ArrayList<>()).add(fileName);
        //                }
        //                pb.updateTask(i);
        //            }
        //            pb.finishTaskAndClose();
        //        }
    }

    private List<String> extractRegparentAreas(String regparent) {
        var areas = new LinkedHashSet<String>();
        var matcher = REGPARENT_AREA_PATTERN.matcher(regparent);
        while (matcher.find()) {
            areas.add(matcher.group(1));
        }
        return new ArrayList<>(areas);
    }

    private boolean shouldAddToArea(RuleContext ruleContext, String area, GameDB gameDB) {
        return !isAreaGameBlackListed(ruleContext.getPlatformPackTaskConfig().getAreaGameBlackList(), area, gameDB)
                && (BASE_AREAS.contains(area) || "P".equals(gameDB.getClone()));
    }

    private boolean isAreaGameBlackListed(Set<String> areaGameBlackList, String area, GameDB gameDB) {
        if (areaGameBlackList == null || areaGameBlackList.isEmpty()) {
            return false;
        }
        return areaGameBlackList.contains(area + " - " + gameDB.getRomName());
    }

    private void generateGameDbJson(RuleContext ruleContext) {
        var gameNamesByArea = new LinkedHashMap<String, List<String>>();
        ruleContext.getGameDBsByArea().forEach((area, gameDBs) -> gameNamesByArea.put(area, gameDBs.stream()
                                                                                                    .map(this::formatGameDbJsonName)
                                                                                                    .distinct()
                                                                                                    .toList()));

        var platformName = ruleContext.getPlatformName();
        var outputPath = Path.of("src", "main", "resources", "platform", platformName, platformName + "_db.json");
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, GSON.toJson(gameNamesByArea), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("生成平台游戏库 JSON 失败: " + outputPath, e);
        }
    }

    private String formatGameDbJsonName(GameDB gameDB) {
        var nameAlt = gameDB.getName_alt();
        if (nameAlt == null || nameAlt.isBlank()) {
            return gameDB.getRomName();
        }
        return gameDB.getRomName() + " | " + nameAlt.trim();
    }
}

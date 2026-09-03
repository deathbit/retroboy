package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.FinalGame;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.domain.game.WikiGame;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.FileContextUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public class RenameHandler {

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    public void handle(PlatformContext platformContext) throws Exception {
        var renameOptionMap = parseRenameOptions(platformContext);
        var renameResultByArea = new LinkedHashMap<String, Map<String, String>>();
        var gameDBsByArea = buildGameDBsByArea(platformContext);
        var areaMapping = buildAreaMapping(platformContext);
        var fileContextLookupMap = FileContextUtils.buildLookupMap(platformContext.getFileContexts());
        gameDBsByArea.forEach((area, gameDbs) -> {
            var renamePlan = buildRenamePlan(fileContextLookupMap, gameDBsByArea, area, renameOptionMap);
            var renameResult = new LinkedHashMap<String, String>();
            ProgressBar pb = new ProgressBar("命名游戏");
            pb.startTask(renamePlan.size());
            for (int i = 0; i < renamePlan.size(); i++) {
                var entry = renamePlan.get(i);
                var oldName = entry.oldName();
                var newName = entry.newName();
                renameResult.put(oldName, removeExtension(newName));
                if (!oldName.equals(newName)) {
                    fileComponent.rename(PathUtils.esdeAreaRom(platformContext, area, oldName), newName);
                }
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
            renameResultByArea.put(area, renameResult);
        });
        populateRenameResultsByMatchResult(platformContext, renameResultByArea);
        platformContext.setFinalGameMapByArea(buildFinalGameMapByArea(platformContext, gameDBsByArea, fileContextLookupMap, areaMapping));
    }

    private void populateRenameResultsByMatchResult(
        PlatformContext platformContext,
        Map<String, Map<String, String>> renameResultByArea
    ) {
        if (platformContext.getMatchResults() == null) {
            throw new IllegalStateException("Match results not found");
        }

        for (var matchResult : platformContext.getMatchResults()) {
            var matchResultRenameResultByArea = new LinkedHashMap<String, Map<String, String>>();
            if (matchResult.getFileContextByArea() == null) {
                matchResult.setRenameResultByArea(matchResultRenameResultByArea);
                continue;
            }
            for (var entry : matchResult.getFileContextByArea().entrySet()) {
                var area = entry.getKey();
                var fileContext = entry.getValue();
                var areaRenameResult = renameResultByArea.get(area);
                if (areaRenameResult == null) {
                    throw new IllegalStateException("Rename result not found for area: " + area);
                }
                var finalRomName = areaRenameResult.get(fileContext.getFileName());
                if (finalRomName == null || finalRomName.isBlank()) {
                    throw new IllegalStateException("Rename result not found for area=%s, file=%s"
                        .formatted(area, fileContext.getFileName()));
                }
                matchResultRenameResultByArea.computeIfAbsent(area, ignored -> new LinkedHashMap<>())
                                           .put(fileContext.getFileName(), finalRomName);
            }
            matchResult.setRenameResultByArea(matchResultRenameResultByArea);
        }
    }

    private Map<String, List<NoIntroGame>> buildGameDBsByArea(PlatformContext platformContext) {
        if (platformContext.getMatchResults() == null) {
            throw new IllegalStateException("Match results not found");
        }

        var gameDBsByArea = new LinkedHashMap<String, List<NoIntroGame>>();
        for (var matchResult : platformContext.getMatchResults()) {
            var pkg = matchResult.getNoIntroGamePackage();
            if (pkg == null || pkg.getNoIntroGameByArea() == null) {
                continue;
            }
            pkg.getNoIntroGameByArea().forEach((area, gameDB) ->
                gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB));
        }
        return gameDBsByArea;
    }

    private Map<String, Map<String, FinalGame>> buildFinalGameMapByArea(
        PlatformContext platformContext,
        Map<String, List<NoIntroGame>> gameDBsByArea,
        Map<String, FileContext> fileContextLookupMap,
        Map<String, String> areaMapping
    ) {
        var finalGameMapByArea = new LinkedHashMap<String, Map<String, FinalGame>>();
        gameDBsByArea.forEach((gameArea, gameDBs) -> {
            var finalGames = new LinkedHashMap<String, FinalGame>();
            for (var gameDB : gameDBs) {
                var fileContext = FileContextUtils.requireFileContext(fileContextLookupMap, gameDB.getTitle());
                var matchResult = findMatchResultForGame(platformContext, gameArea, gameDB);
                var renameResult = matchResult.getRenameResultByArea() == null
                    ? null
                    : matchResult.getRenameResultByArea().get(gameArea);
                if (renameResult == null) {
                    throw new IllegalStateException("Rename result not found for area: " + gameArea);
                }

                var finalRomName = renameResult.get(fileContext.getFileName());
                if (finalRomName == null || finalRomName.isBlank()) {
                    throw new IllegalStateException("Final ROM name not found for area=%s, rom=%s"
                        .formatted(gameArea, gameDB.getTitle()));
                }

                var wikiDB = findWikiGameForGame(matchResult, areaMapping, gameArea, gameDB);
                var finalGame = FinalGame.builder()
                                         .finalRomName(finalRomName)
                                         .originRomName(fileContext.getFullName())
                                         .wikiArea(wikiDB.getArea())
                                         .wikiName(wikiDB.getTitle())
                                         .gameArea(gameArea)
                                         .gameName(gameDB.getName())
                                         .wikiGame(wikiDB)
                                         .noIntroGame(gameDB)
                                         .fileContext(fileContext)
                                         .build();
                var existing = finalGames.putIfAbsent(finalRomName, finalGame);
                if (existing != null) {
                    throw new IllegalStateException("Final ROM name conflict: area=%s, finalRomName=%s, rom1=%s, rom2=%s"
                        .formatted(gameArea, finalRomName, existing.getOriginRomName(), finalGame.getOriginRomName()));
                }
            }
            finalGameMapByArea.put(gameArea, finalGames);
        });
        return finalGameMapByArea;
    }

    private MatchResult findMatchResultForGame(PlatformContext platformContext, String gameArea, NoIntroGame noIntroGame) {
        if (platformContext.getMatchResults() == null) {
            throw new IllegalStateException("Match results not found");
        }

        MatchResult foundMatchResult = null;
        for (var matchResult : platformContext.getMatchResults()) {
            var noIntroGamePackage = matchResult.getNoIntroGamePackage();
            if (noIntroGamePackage == null || noIntroGamePackage.getNoIntroGameByArea() == null) {
                continue;
            }
            var candidate = noIntroGamePackage.getNoIntroGameByArea().get(gameArea);
            if (candidate != noIntroGame) {
                continue;
            }
            if (foundMatchResult != null) {
                throw new IllegalStateException("Multiple match results found for area=%s, rom=%s"
                    .formatted(gameArea, noIntroGame.getTitle()));
            }
            foundMatchResult = matchResult;
        }
        if (foundMatchResult == null) {
            throw new IllegalStateException("Match result not found for area=%s, rom=%s"
                .formatted(gameArea, noIntroGame.getTitle()));
        }
        return foundMatchResult;
    }

    private WikiGame findWikiGameForGame(
        MatchResult matchResult,
        Map<String, String> areaMapping,
        String gameArea,
        NoIntroGame noIntroGame
    ) {
        var wikiGamePackage = matchResult.getWikiGamePackage();
        var wikiArea = areaMapping.getOrDefault(gameArea, gameArea);
        var wikiGame = wikiGamePackage == null || wikiGamePackage.getWikiGameByArea() == null
            ? null
            : wikiGamePackage.getWikiGameByArea().get(wikiArea);
        if (wikiGame == null) {
            throw new IllegalStateException("Wiki game not found for area=%s, wikiArea=%s, rom=%s"
                .formatted(gameArea, wikiArea, noIntroGame.getTitle()));
        }
        return wikiGame;
    }

    private Map<String, String> buildAreaMapping(PlatformContext platformContext) {
        return getPlatformProcessor(platformContext).gameDBToWikiDBAreaMapping(
            buildGameAreas(platformContext), buildWikiAreas(platformContext));
    }

    private List<String> buildGameAreas(PlatformContext platformContext) {
        var areas = new LinkedHashSet<String>();
        for (var matchResult : platformContext.getMatchResults()) {
            var pkg = matchResult.getNoIntroGamePackage();
            if (pkg != null && pkg.getNoIntroGameByArea() != null) {
                areas.addAll(pkg.getNoIntroGameByArea().keySet());
            }
        }
        return new ArrayList<>(areas);
    }

    private List<String> buildWikiAreas(PlatformContext platformContext) {
        var areas = new LinkedHashSet<String>();
        for (var matchResult : platformContext.getMatchResults()) {
            var pkg = matchResult.getWikiGamePackage();
            if (pkg != null && pkg.getWikiGameByArea() != null) {
                areas.addAll(pkg.getWikiGameByArea().keySet());
            }
        }
        return new ArrayList<>(areas);
    }

    private PlatformProcessor getPlatformProcessor(PlatformContext platformContext) {
        var platformProcessor = platformProcessorMap.get(platformContext.getPlatform());
        if (platformProcessor == null) {
            throw new IllegalStateException("PlatformProcessor not found for platform: " + platformContext.getPlatform());
        }
        return platformProcessor;
    }

    private Map<String, String> parseRenameOptions(PlatformContext platformContext) {
        var renameOptions = platformContext.getPlatformPackTaskConfig().getRenameOptions();
        if (renameOptions == null || renameOptions.isEmpty()) {
            return Map.of();
        }

        var renameOptionMap = new LinkedHashMap<String, String>();
        for (var option : renameOptions) {
            var parts = option.split("\\s*->\\s*", 2);
            if (parts.length != 2) {
                throw new RuntimeException("renameOptions 格式错误: " + option);
            }
            var oldName = parts[0].trim();
            var newName = parts[1].trim();
            if (oldName.isEmpty() || newName.isEmpty()) {
                throw new RuntimeException("renameOptions 不能为空: " + option);
            }
            var previous = renameOptionMap.putIfAbsent(oldName, newName);
            if (previous != null) {
                throw new RuntimeException("renameOptions 中 oldName 重复: " + oldName);
            }
        }
        return renameOptionMap;
    }

    private List<RenamePlan> buildRenamePlan(
        Map<String, FileContext> fileContextLookupMap,
        Map<String, List<NoIntroGame>> gameDBsByArea,
        String area,
        Map<String, String> renameOptionMap
    ) {
        var renamePlan = new ArrayList<RenamePlan>();
        var targetToSource = new HashMap<String, String>();
        var gameDbs = gameDBsByArea.getOrDefault(area, List.of());
        for (var gameDB : gameDbs) {
            var fileContext = FileContextUtils.requireFileContext(fileContextLookupMap, gameDB.getTitle());
            var oldName = fileContext.getFileName();
            var newName = renameOptionMap.getOrDefault(oldName, buildNewName(fileContext));
            var existingSource = targetToSource.putIfAbsent(newName, oldName);
            if (existingSource != null && !existingSource.equals(oldName)) {
                throw new RuntimeException("重命名目标冲突: area=%s, target=%s, source1=%s, source2=%s，请在 renameOptions 中配置不同目标名"
                    .formatted(area, newName, existingSource, oldName));
            }
            renamePlan.add(new RenamePlan(oldName, newName));
        }
        return renamePlan;
    }

    private String buildNewName(FileContext fileContext) {
        return normalizeLeadingArticle(fileContext.getNamePart()) + fileContext.getExtension();
    }

    private String removeExtension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private String normalizeLeadingArticle(String namePart) {
        return normalizeTrailingArticle(normalizeTrailingArticle(namePart, "The"), "A");
    }

    private String normalizeTrailingArticle(String namePart, String article) {
        var suffix = ", " + article;
        var separatorIndex = namePart.indexOf(" - ");
        if (separatorIndex == -1) {
            if (namePart.endsWith(suffix)) {
                return article + " " + namePart.substring(0, namePart.length() - suffix.length());
            }
            return namePart;
        }

        var title = namePart.substring(0, separatorIndex);
        if (!title.endsWith(suffix)) {
            return namePart;
        }

        return article + " " + title.substring(0, title.length() - suffix.length()) + namePart.substring(separatorIndex);
    }

    private record RenamePlan(String oldName, String newName) {
    }
}

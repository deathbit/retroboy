package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.FinalGame;
import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.domain.MatchPairForGame;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.util.FileContextUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RenameHandler {

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) throws Exception {
        var renameOptionMap = parseRenameOptions(platformContext);
        var renameResultByArea = new LinkedHashMap<String, Map<String, String>>();
        var gameDBsByArea = buildGameDBsByArea(platformContext);
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
        platformContext.setRenameResultByArea(renameResultByArea);
        platformContext.setFinalGameMapByArea(buildFinalGameMapByArea(platformContext, gameDBsByArea, fileContextLookupMap));
    }

    private Map<String, List<NoIntroGame>> buildGameDBsByArea(PlatformContext platformContext) {
        var gameDBsByArea = new LinkedHashMap<String, List<NoIntroGame>>();
        for (var pkg : platformContext.getNoIntroGamePackages()) {
            pkg.getNoIntroGameByArea().forEach((area, gameDB) ->
                gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB));
        }
        return gameDBsByArea;
    }

    private Map<String, Map<String, FinalGame>> buildFinalGameMapByArea(
        PlatformContext platformContext,
        Map<String, List<NoIntroGame>> gameDBsByArea,
        Map<String, FileContext> fileContextLookupMap
    ) {
        var finalGameMapByArea = new LinkedHashMap<String, Map<String, FinalGame>>();
        gameDBsByArea.forEach((gameArea, gameDBs) -> {
            var renameResult = platformContext.getRenameResultByArea().get(gameArea);
            if (renameResult == null) {
                throw new IllegalStateException("Rename result not found for area: " + gameArea);
            }

            var finalGames = new LinkedHashMap<String, FinalGame>();
            for (var gameDB : gameDBs) {
                var fileContext = FileContextUtils.requireFileContext(fileContextLookupMap, gameDB.getTitle());

                var finalRomName = renameResult.get(fileContext.getFileName());
                if (finalRomName == null || finalRomName.isBlank()) {
                    throw new IllegalStateException("Final ROM name not found for area=%s, rom=%s"
                        .formatted(gameArea, gameDB.getTitle()));
                }

                var matchPair = findMatchPairForGame(platformContext, gameArea, gameDB);
                var wikiDB = matchPair.getWikiGame();
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

    private MatchPairForGame findMatchPairForGame(PlatformContext platformContext, String gameArea, NoIntroGame noIntroGame) {
        var areaMapping = platformContext.getMatchResult().getGameDBToWikiDBAreaMapping();
        var wikiArea = areaMapping.getOrDefault(gameArea, gameArea);
        var matchPairs = platformContext.getMatchPairForGamesByArea().get(wikiArea);
        if (matchPairs == null) {
            throw new IllegalStateException("Match pairs not found for wiki area: " + wikiArea);
        }

        MatchPairForGame matchPair = null;
        for (var candidate : matchPairs) {
            if (candidate.getNoIntroGame() != noIntroGame) {
                continue;
            }
            if (matchPair != null) {
                throw new IllegalStateException("Multiple match pairs found for area=%s, rom=%s"
                    .formatted(gameArea, noIntroGame.getTitle()));
            }
            matchPair = candidate;
        }
        if (matchPair == null || matchPair.getWikiGame() == null) {
            throw new IllegalStateException("Match pair not found for area=%s, rom=%s"
                .formatted(gameArea, noIntroGame.getTitle()));
        }
        return matchPair;
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

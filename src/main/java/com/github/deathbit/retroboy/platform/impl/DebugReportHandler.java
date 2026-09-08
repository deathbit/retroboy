package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.FinalGame;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.MediaCompletionRate;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.util.MediaBitmapUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Component
public class DebugReportHandler {

    private static final String MEDIA_EXISTS = "●";
    private static final String MEDIA_MISSING = "○";

    public void handle(PlatformContext platformContext) {
        var debugReportPath = PathUtils.DEBUG_REPORT.get(platformContext);
        var content = new StringBuilder();
        content.append(System.lineSeparator());
        appendFileContexts(content, platformContext.getFileContexts());
        appendFinalGames(content, platformContext.getFinalGameMapByArea());
        appendRenameResults(content, platformContext.getMatchResults());
        appendMediaCompletionRates(content, platformContext.getMediaCompletionRateMap());
        appendSSMediaRegionSummary(content, platformContext);
        appendMissingMediaResults(content, platformContext.getFinalGameMapByArea());
        appendMissingMediaResultsByType(content, platformContext);
        appendGameMappings(content, platformContext.getFinalGameMapByArea());

        try {
            Files.createDirectories(debugReportPath.getParent());
            Files.writeString(debugReportPath, buildReportWithDirectory(content), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write debug report: " + debugReportPath, e);
        }
    }

    private String buildReportWithDirectory(StringBuilder content) {
        var body = content.toString();
        var directoryEntries = java.util.Arrays.stream(body.split("\\R"))
                .filter(line -> line.endsWith("："))
                .filter(line -> !line.startsWith("媒体顺序："))
                .map(line -> line.substring(0, line.length() - 1))
                .toList();

        var report = new StringBuilder();
        report.append("目录").append(System.lineSeparator());
        directoryEntries.forEach(entry -> report.append(entry).append(System.lineSeparator()));
        report.append(System.lineSeparator()).append(body);
        return report.toString();
    }

    private void appendFileContexts(StringBuilder content, List<FileContext> fileContexts) {
        var sortedFileContexts = (fileContexts == null ? List.<FileContext>of() : fileContexts).stream()
                .sorted(Comparator.comparing(FileContext::getFileName))
                .toList();
        content.append("原始ROM信息 - ").append(sortedFileContexts.size()).append("：").append(System.lineSeparator());
        content.append("fileName | fullName | aliasNames | namePart | tagPart | tags | extension").append(System.lineSeparator());
        sortedFileContexts.forEach(fileContext -> content.append(fileContext.getFileName())
                .append(" | ").append(fileContext.getFullName())
                .append(" | ").append(formatTags(fileContext.getAliasNames()))
                .append(" | ").append(fileContext.getNamePart())
                .append(" | ").append(fileContext.getTagPart())
                .append(" | ").append(formatTags(fileContext.getTags()))
                .append(" | ").append(fileContext.getExtension())
                .append(System.lineSeparator()));
        content.append(System.lineSeparator());
    }

    private void appendFinalGames(StringBuilder content, Map<String, Map<String, FinalGame>> finalGameMapByArea) {
        finalGameMapByArea.forEach((area, finalGames) -> {
            var games = sortedFinalGames(finalGames);
            content.append("最终游戏列表 - ").append(area).append(" - ").append(games.size()).append("：")
                    .append(System.lineSeparator());
            games.forEach(game -> content.append(game.getFinalRomName()).append(System.lineSeparator()));
            content.append(System.lineSeparator());
        });
    }

    private void appendRenameResults(StringBuilder content, List<MatchResult> matchResults) {
        var renameResultByArea = aggregateRenameResultsByArea(matchResults);
        renameResultByArea.forEach((area, renameResults) -> {
            content.append("重命名结果 - ").append(area).append(" - ").append(renameResults.size()).append("：")
                    .append(System.lineSeparator());
            renameResults.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> content.append(entry.getKey()).append(" -> ").append(entry.getValue())
                            .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        });
    }

    private Map<String, Map<String, String>> aggregateRenameResultsByArea(List<MatchResult> matchResults) {
        var renameResultByArea = new LinkedHashMap<String, Map<String, String>>();
        for (var matchResult : matchResults == null ? List.<MatchResult>of() : matchResults) {
            if (matchResult.getRenameResultByArea() == null) {
                continue;
            }
            matchResult.getRenameResultByArea().forEach((area, renameResults) ->
                    renameResultByArea.computeIfAbsent(area, ignored -> new LinkedHashMap<>()).putAll(renameResults));
        }
        return renameResultByArea;
    }

    private void appendMediaCompletionRates(StringBuilder content,
                                            Map<String, Map<MediaAssetType, MediaCompletionRate>> mediaCompletionRateMap) {
        mediaCompletionRateMap.forEach((area, rates) -> {
            content.append("媒体缺失率 - ").append(area).append(" - ").append(rates.size()).append("：")
                    .append(System.lineSeparator());
            for (var mediaAssetType : MediaAssetType.values()) {
                var rate = rates.get(mediaAssetType);
                if (rate != null) {
                    content.append(mediaAssetType.getDirectoryName()).append("：")
                            .append(rate.getCompletedCount()).append("/").append(rate.getTotalCount())
                            .append("（").append(formatCompletionRate(rate)).append("）")
                            .append(System.lineSeparator());
                }
            }
            content.append(System.lineSeparator());
        });
    }

    private void appendSSMediaRegionSummary(StringBuilder content, PlatformContext platformContext) {
        var regionsByMediaType = collectSSMediaRegionsByType(platformContext);
        content.append("ScreenScraper媒体地区统计 - ").append(regionsByMediaType.size()).append("：")
                .append(System.lineSeparator());
        content.append("mediaType | regions").append(System.lineSeparator());
        regionsByMediaType.forEach((mediaType, regions) -> content.append(mediaType)
                .append(" | ").append(regions)
                .append(System.lineSeparator()));
        content.append(System.lineSeparator());
    }

    private Map<String, Set<String>> collectSSMediaRegionsByType(PlatformContext platformContext) {
        var ssRoot = PathUtils.PLATFORM_RESOURCE_ROOT.get(platformContext).resolve("ss");
        var regionsByMediaType = new TreeMap<String, Set<String>>();
        if (Files.notExists(ssRoot)) {
            return regionsByMediaType;
        }

        try (var gameDirectories = Files.list(ssRoot)) {
            gameDirectories.filter(Files::isDirectory)
                    .forEach(gameDirectory -> collectSSMediaRegionsByType(gameDirectory, regionsByMediaType));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to collect ScreenScraper media regions: " + ssRoot, e);
        }
        return regionsByMediaType;
    }

    private void collectSSMediaRegionsByType(Path gameDirectory, Map<String, Set<String>> regionsByMediaType) {
        var gameId = gameDirectory.getFileName().toString();
        try (var mediaFiles = Files.list(gameDirectory)) {
            mediaFiles.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> parseSSMediaRegion(gameId, fileName))
                    .filter(Objects::nonNull)
                    .forEach(mediaRegion -> regionsByMediaType
                            .computeIfAbsent(mediaRegion.mediaType(), ignored -> new TreeSet<>())
                            .add(mediaRegion.region()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to collect ScreenScraper media regions: " + gameDirectory, e);
        }
    }

    private SSMediaRegion parseSSMediaRegion(String gameId, String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return null;
        }

        var baseName = fileName.substring(0, dotIndex);
        var prefix = gameId + "_";
        if (!baseName.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return null;
        }

        var mediaTypeAndRegion = baseName.substring(prefix.length());
        var regionSeparatorIndex = mediaTypeAndRegion.lastIndexOf('_');
        if (regionSeparatorIndex <= 0 || regionSeparatorIndex == mediaTypeAndRegion.length() - 1) {
            return null;
        }

        var mediaType = mediaTypeAndRegion.substring(0, regionSeparatorIndex).toLowerCase(Locale.ROOT);
        var region = mediaTypeAndRegion.substring(regionSeparatorIndex + 1).toUpperCase(Locale.ROOT);
        return new SSMediaRegion(mediaType, region);
    }

    private void appendMissingMediaResults(StringBuilder content, Map<String, Map<String, FinalGame>> finalGameMapByArea) {
        finalGameMapByArea.forEach((area, finalGames) -> {
            var missingMediaGames = sortedFinalGames(finalGames).stream()
                    .filter(game -> game.getMediaBitMap() != allMediaBitMask())
                    .toList();
            content.append("媒体缺失列表 - ").append(area).append(" - ").append(missingMediaGames.size()).append("：")
                    .append(System.lineSeparator());
            content.append("mediaStatus | wikiName | finalName | missingMedia").append(System.lineSeparator());
            content.append("媒体顺序：").append(formatMediaAssetOrder()).append(System.lineSeparator());
            missingMediaGames.forEach(game -> content.append(formatMediaStatus(game))
                    .append(" | ").append(game.getWikiName())
                    .append(" | ").append(game.getFinalRomName())
                    .append(" | ").append(formatMissingMedia(game))
                    .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        });
    }

    private void appendMissingMediaResultsByType(StringBuilder content, PlatformContext platformContext) {
        var finalGameMapByArea = platformContext.getFinalGameMapByArea();
        var ssPackageIdByAreaAndFinalName = buildSSPackageIdByAreaAndFinalName(platformContext.getMatchResults());
        var sourceRegionsByPackageIdAndMediaType = collectSSMediaRegionsByPackageIdAndType(platformContext);

        content.append("媒体缺失列表 - 按媒体类型：").append(System.lineSeparator());
        for (var mediaAssetType : MediaAssetType.values()) {
            if (mediaAssetType == MediaAssetType.MIX_IMAGE) {
                continue;
            }

            var missingMediaGames = collectMissingMediaGames(finalGameMapByArea, mediaAssetType);
            content.append(mediaAssetType.getDirectoryName()).append(" - ").append(missingMediaGames.size()).append("：")
                    .append(System.lineSeparator());
            content.append("area | ssPackageId | sourceRegions | wikiName | finalName").append(System.lineSeparator());
            missingMediaGames.forEach(game -> {
                var ssPackageId = resolveSSPackageId(ssPackageIdByAreaAndFinalName, game);
                content.append(game.area())
                        .append(" | ").append(ssPackageId)
                        .append(" | ").append(formatSourceRegions(sourceRegionsByPackageIdAndMediaType, ssPackageId, mediaAssetType))
                        .append(" | ").append(game.finalGame().getWikiName())
                        .append(" | ").append(game.finalGame().getFinalRomName())
                        .append(System.lineSeparator());
            });
            content.append(System.lineSeparator());
        }
    }

    private List<MissingMediaGame> collectMissingMediaGames(Map<String, Map<String, FinalGame>> finalGameMapByArea,
                                                            MediaAssetType mediaAssetType) {
        return finalGameMapByArea.entrySet().stream()
                .flatMap(entry -> entry.getValue().values().stream()
                        .filter(game -> MediaBitmapUtils.isMediaMissing(game.getMediaBitMap(), mediaAssetType))
                        .map(game -> new MissingMediaGame(entry.getKey(), game)))
                .sorted(Comparator.comparing(MissingMediaGame::area)
                        .thenComparing(game -> game.finalGame().getFinalRomName()))
                .toList();
    }

    private Map<String, Map<String, String>> buildSSPackageIdByAreaAndFinalName(List<MatchResult> matchResults) {
        var ssPackageIdByAreaAndFinalName = new LinkedHashMap<String, Map<String, String>>();
        for (var matchResult : matchResults == null ? List.<MatchResult>of() : matchResults) {
            if (matchResult.getFileContextByArea() == null || matchResult.getRenameResultByArea() == null) {
                continue;
            }

            for (var entry : matchResult.getFileContextByArea().entrySet()) {
                var area = entry.getKey();
                var fileContext = entry.getValue();
                var renameResult = matchResult.getRenameResultByArea().get(area);
                if (renameResult == null) {
                    continue;
                }

                var finalName = renameResult.get(fileContext.getFileName());
                var ssPackageId = findSSPackageId(matchResult, area);
                if (finalName != null && !finalName.isBlank() && ssPackageId != null && !ssPackageId.isBlank()) {
                    ssPackageIdByAreaAndFinalName
                            .computeIfAbsent(area, ignored -> new LinkedHashMap<>())
                            .put(finalName, ssPackageId);
                }
            }
        }
        return ssPackageIdByAreaAndFinalName;
    }

    private String findSSPackageId(MatchResult matchResult, String area) {
        var ssGamePackageByArea = matchResult.getSsGamePackageByArea();
        if (ssGamePackageByArea == null || ssGamePackageByArea.isEmpty()) {
            return "";
        }

        var ssGamePackage = ssGamePackageByArea.get(area);
        if (ssGamePackage == null) {
            ssGamePackage = ssGamePackageByArea.values().stream()
                    .filter(candidate -> candidate != null
                            && candidate.getSsGameByArea() != null
                            && candidate.getSsGameByArea().containsKey(area))
                    .findFirst()
                    .orElse(null);
        }
        return ssGamePackage == null ? "" : ssGamePackage.getId();
    }

    private String resolveSSPackageId(Map<String, Map<String, String>> ssPackageIdByAreaAndFinalName,
                                      MissingMediaGame game) {
        return ssPackageIdByAreaAndFinalName
                .getOrDefault(game.area(), Map.of())
                .getOrDefault(game.finalGame().getFinalRomName(), "");
    }

    private Map<String, Map<String, Set<String>>> collectSSMediaRegionsByPackageIdAndType(PlatformContext platformContext) {
        var ssRoot = PathUtils.PLATFORM_RESOURCE_ROOT.get(platformContext).resolve("ss");
        var regionsByPackageIdAndMediaType = new TreeMap<String, Map<String, Set<String>>>();
        if (Files.notExists(ssRoot)) {
            return regionsByPackageIdAndMediaType;
        }

        try (var gameDirectories = Files.list(ssRoot)) {
            gameDirectories.filter(Files::isDirectory)
                    .forEach(gameDirectory -> collectSSMediaRegionsByPackageIdAndType(gameDirectory, regionsByPackageIdAndMediaType));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to collect ScreenScraper media regions: " + ssRoot, e);
        }
        return regionsByPackageIdAndMediaType;
    }

    private void collectSSMediaRegionsByPackageIdAndType(Path gameDirectory,
                                                         Map<String, Map<String, Set<String>>> regionsByPackageIdAndMediaType) {
        var gameId = gameDirectory.getFileName().toString();
        try (var mediaFiles = Files.list(gameDirectory)) {
            mediaFiles.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> parseSSMediaRegion(gameId, fileName))
                    .filter(Objects::nonNull)
                    .forEach(mediaRegion -> regionsByPackageIdAndMediaType
                            .computeIfAbsent(gameId, ignored -> new TreeMap<>())
                            .computeIfAbsent(mediaRegion.mediaType(), ignored -> new TreeSet<>())
                            .add(mediaRegion.region()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to collect ScreenScraper media regions: " + gameDirectory, e);
        }
    }

    private String formatSourceRegions(Map<String, Map<String, Set<String>>> sourceRegionsByPackageIdAndMediaType,
                                       String ssPackageId,
                                       MediaAssetType mediaAssetType) {
        if (ssPackageId == null || ssPackageId.isBlank()) {
            return "[]";
        }

        var sourceRegionsByType = sourceRegionsByPackageIdAndMediaType.getOrDefault(ssPackageId, Map.of());
        var sourceMediaTypes = sourceMediaTypes(mediaAssetType);
        if (sourceMediaTypes.size() == 1) {
            return sourceRegionsByType.getOrDefault(sourceMediaTypes.get(0), Set.of()).toString();
        }

        return sourceMediaTypes.stream()
                .map(sourceMediaType -> sourceMediaType + "=" + sourceRegionsByType.getOrDefault(sourceMediaType, Set.of()))
                .toList()
                .toString();
    }

    private List<String> sourceMediaTypes(MediaAssetType mediaAssetType) {
        return switch (mediaAssetType) {
            case THREE_D_BOX -> List.of("box-3d");
            case BACK_COVER -> List.of("box-2d-back");
            case COVER -> List.of("box-2d");
            case FANART -> List.of("fanart");
            case MANUAL -> List.of("manuel");
            case MARQUEE -> List.of("wheel", "wheel-hd");
            case MIX_IMAGE -> List.of("miximage");
            case PHYSICAL_MEDIA -> List.of("support-2d");
            case SCREENSHOT -> List.of("ss");
            case TITLE_SCREEN -> List.of("sstitle");
            case VIDEO -> List.of("video");
        };
    }

    private void appendGameMappings(StringBuilder content, Map<String, Map<String, FinalGame>> finalGameMapByArea) {
        finalGameMapByArea.forEach((area, finalGames) -> {
            var games = sortedFinalGames(finalGames);
            content.append("维基百科名称映射列表 - ").append(area).append(" - ").append(games.size()).append("：")
                    .append(System.lineSeparator());
            content.append("wikiName | oldName | newName | finalName").append(System.lineSeparator());
            games.forEach(game -> content.append(game.getWikiName())
                    .append(" | ").append(game.getOriginRomName())
                    .append(" | ").append(game.getFinalRomName())
                    .append(" | ").append(game.getFinalRomName())
                    .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        });
    }

    private List<FinalGame> sortedFinalGames(Map<String, FinalGame> finalGames) {
        return finalGames.values().stream().sorted(Comparator.comparing(FinalGame::getFinalRomName)).toList();
    }

    private int allMediaBitMask() {
        var bitmap = 0;
        for (var mediaAssetType : MediaAssetType.values()) {
            bitmap = MediaBitmapUtils.withMedia(bitmap, mediaAssetType);
        }
        return bitmap;
    }

    private String formatMediaStatus(FinalGame game) {
        var status = new StringBuilder();
        for (var mediaAssetType : MediaAssetType.values()) {
            status.append(MediaBitmapUtils.hasMedia(game.getMediaBitMap(), mediaAssetType) ? MEDIA_EXISTS : MEDIA_MISSING);
        }
        return status.toString();
    }

    private String formatMissingMedia(FinalGame game) {
        return java.util.Arrays.stream(MediaAssetType.values())
                .filter(mediaAssetType -> MediaBitmapUtils.isMediaMissing(game.getMediaBitMap(), mediaAssetType))
                .map(MediaAssetType::getDirectoryName)
                .toList()
                .toString();
    }

    private String formatMediaAssetOrder() {
        return java.util.Arrays.stream(MediaAssetType.values())
                .map(MediaAssetType::getDirectoryName)
                .toList()
                .toString();
    }

    private String formatCompletionRate(MediaCompletionRate rate) {
        var percentage = rate.getCompletionRate() * 100;
        return percentage == Math.rint(percentage)
                ? String.format(Locale.ROOT, "%.0f%%", percentage)
                : String.format(Locale.ROOT, "%.2f%%", percentage);
    }

    private String formatTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        return tags.stream().sorted().toList().toString();
    }

    private record SSMediaRegion(String mediaType, String region) {
    }

    private record MissingMediaGame(String area, FinalGame finalGame) {
    }
}

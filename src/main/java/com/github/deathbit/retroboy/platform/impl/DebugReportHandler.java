package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.FinalGame;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        appendRenameResults(content, platformContext.getRenameResultByArea());
        appendMediaCompletionRates(content, platformContext.getMediaCompletionRateMap());
        appendMissingMediaResults(content, platformContext.getFinalGameMapByArea());
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

    private void appendRenameResults(StringBuilder content, Map<String, Map<String, String>> renameResultByArea) {
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
}

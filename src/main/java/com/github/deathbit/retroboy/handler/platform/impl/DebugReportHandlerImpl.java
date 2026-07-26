package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.domain.AreaRenameResult;
import com.github.deathbit.retroboy.domain.AreaRuleResult;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.domain.WikiGameEntry;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.handler.platform.DebugReportHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DebugReportHandlerImpl implements DebugReportHandler {

    private static final String MEDIA_EXISTS = "●";
    private static final String MEDIA_MISSING = "○";

    @Override
    public void handle(RuleContext ruleContext) {
        var debugReportPath = Path.of(ruleContext.getGlobalConfig().getResourcesHomePath(),
                "platform",
                ruleContext.getPlatformName(),
                "report",
                String.format("调试信息-%s.txt", ruleContext.getPlatform().name()));
        var content = new StringBuilder();
        appendLicensedGames(content, ruleContext.getLicensed());
        content.append(System.lineSeparator());
        appendFileContexts(content, ruleContext.getFileContextMap());
        content.append(System.lineSeparator());
        appendAreaPassGames(content, ruleContext.getAreaPassMap());
        appendAreaNotPassGames(content, ruleContext.getAreaRuleResultMap());
        appendAreaRenameResults(content, ruleContext.getAreaRenameResultMap());
        appendMissingMediaResults(content, ruleContext.getAreaWikiEntryMap());
        appendWikiNameMappings(content, ruleContext.getAreaWikiEntryMap());

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
        report.append("目录")
                .append(System.lineSeparator());
        directoryEntries.forEach(directoryEntry -> report.append(directoryEntry).append(System.lineSeparator()));
        report.append(System.lineSeparator())
                .append(body);
        return report.toString();
    }

    private void appendLicensedGames(StringBuilder content, Set<String> licensedGames) {
        var sortedLicensedGames = licensedGames.stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        content.append("授权游戏列表 - ")
                .append(sortedLicensedGames.size())
                .append("：")
                .append(System.lineSeparator());
        sortedLicensedGames.forEach(licensedGame -> content.append(licensedGame).append(System.lineSeparator()));
    }

    private void appendFileContexts(StringBuilder content, Map<String, FileContext> fileContextMap) {
        var sortedFileContexts = fileContextMap.values()
                .stream()
                .sorted(Comparator.comparing(FileContext::getFileName))
                .toList();
        content.append("原始ROM信息 - ")
                .append(sortedFileContexts.size())
                .append("：")
                .append(System.lineSeparator());
        content.append("fileName | fullName | namePart | tagPart | tags | extension")
                .append(System.lineSeparator());
        sortedFileContexts.forEach(fileContext -> content.append(fileContext.getFileName())
                .append(" | ")
                .append(fileContext.getFullName())
                .append(" | ")
                .append(fileContext.getNamePart())
                .append(" | ")
                .append(fileContext.getTagPart())
                .append(" | ")
                .append(formatTags(fileContext.getTags()))
                .append(" | ")
                .append(fileContext.getExtension())
                .append(System.lineSeparator()));
    }

    private void appendAreaPassGames(StringBuilder content, Map<Area, List<String>> areaPassMap) {
        for (var area : Area.values()) {
            var sortedGames = areaPassMap.getOrDefault(area, List.of())
                    .stream()
                    .sorted(Comparator.naturalOrder())
                    .toList();
            content.append("通过游戏列表 - ")
                    .append(area.name())
                    .append(" - ")
                    .append(sortedGames.size())
                    .append("：")
                    .append(System.lineSeparator());
            sortedGames.forEach(game -> content.append(game).append(System.lineSeparator()));
            content.append(System.lineSeparator());
        }
    }

    private void appendAreaNotPassGames(StringBuilder content, Map<Area, Map<String, AreaRuleResult>> areaRuleResultMap) {
        for (var area : Area.values()) {
            var sortedRuleResults = areaRuleResultMap.getOrDefault(area, Map.of())
                    .values()
                    .stream()
                    .filter(ruleResult -> !ruleResult.isPassed())
                    .sorted(Comparator.comparing(AreaRuleResult::getFileName))
                    .toList();
            content.append("未通过游戏列表 - ")
                    .append(area.name())
                    .append(" - ")
                    .append(sortedRuleResults.size())
                    .append("：")
                    .append(System.lineSeparator());
            content.append("游戏名 | 原因")
                    .append(System.lineSeparator());
            sortedRuleResults.forEach(ruleResult -> content.append(ruleResult.getFileName())
                    .append(" | ")
                    .append(formatReasons(ruleResult.getReasons()))
                    .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        }
    }

    private void appendAreaRenameResults(StringBuilder content, Map<Area, Map<String, AreaRenameResult>> areaRenameResultMap) {
        for (var area : Area.values()) {
            var sortedRenameResults = areaRenameResultMap.getOrDefault(area, Map.of())
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(AreaRenameResult::getOldName))
                    .toList();
            content.append("重命名结果 - ")
                    .append(area.name())
                    .append(" - ")
                    .append(sortedRenameResults.size())
                    .append("：")
                    .append(System.lineSeparator());
            sortedRenameResults.forEach(renameResult -> content.append(renameResult.getOldName())
                    .append(" -> ")
                    .append(renameResult.getNewName())
                    .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        }
    }

    private void appendMissingMediaResults(StringBuilder content, Map<Area, Map<String, WikiGameEntry>> areaWikiEntryMap) {
        for (var area : Area.values()) {
            var sortedMissingMediaEntries = areaWikiEntryMap.getOrDefault(area, Map.of())
                    .values()
                    .stream()
                    .filter(wikiGameEntry -> wikiGameEntry.getMissingMediaBitmap() != 0)
                    .sorted(Comparator.comparing(WikiGameEntry::getWikiName))
                    .toList();
            content.append("媒体缺失列表 - ")
                    .append(area.name())
                    .append(" - ")
                    .append(sortedMissingMediaEntries.size())
                    .append("：")
                    .append(System.lineSeparator());
            content.append("mediaStatus | wikiName | finalName | missingMedia")
                    .append(System.lineSeparator());
            content.append("媒体顺序：")
                    .append(formatMediaAssetOrder())
                    .append(System.lineSeparator());
            sortedMissingMediaEntries.forEach(wikiGameEntry -> content.append(formatMediaStatus(wikiGameEntry))
                    .append(" | ")
                    .append(wikiGameEntry.getWikiName())
                    .append(" | ")
                    .append(getFinalName(wikiGameEntry))
                    .append(" | ")
                    .append(formatMissingMedia(wikiGameEntry))
                    .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        }
    }

    private void appendWikiNameMappings(StringBuilder content, Map<Area, Map<String, WikiGameEntry>> areaWikiEntryMap) {
        for (var area : Area.values()) {
            var sortedWikiGameEntries = areaWikiEntryMap.getOrDefault(area, Map.of())
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(WikiGameEntry::getWikiName))
                    .toList();
            content.append("维基百科名称映射列表 - ")
                    .append(area.name())
                    .append(" - ")
                    .append(sortedWikiGameEntries.size())
                    .append("：")
                    .append(System.lineSeparator());
            content.append("wikiName | oldName | newName | finalName")
                    .append(System.lineSeparator());
            sortedWikiGameEntries.forEach(wikiGameEntry -> content.append(wikiGameEntry.getWikiName())
                    .append(" | ")
                    .append(getOldName(wikiGameEntry))
                    .append(" | ")
                    .append(getNewName(wikiGameEntry))
                    .append(" | ")
                    .append(getFinalName(wikiGameEntry))
                    .append(System.lineSeparator()));
            content.append(System.lineSeparator());
        }
    }

    private String getOldName(WikiGameEntry wikiGameEntry) {
        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
        if (areaRenameResult == null || areaRenameResult.getOldName() == null) {
            return "";
        }
        return areaRenameResult.getOldName();
    }

    private String getNewName(WikiGameEntry wikiGameEntry) {
        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
        if (areaRenameResult == null || areaRenameResult.getNewName() == null) {
            return "";
        }
        return areaRenameResult.getNewName();
    }

    private String getFinalName(WikiGameEntry wikiGameEntry) {
        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
        if (areaRenameResult == null || areaRenameResult.getFinalName() == null) {
            return "";
        }
        return areaRenameResult.getFinalName();
    }

    private String formatMissingMedia(WikiGameEntry wikiGameEntry) {
        return java.util.Arrays.stream(MediaAssetType.values())
                .filter(wikiGameEntry::isMediaMissing)
                .map(MediaAssetType::getDirectoryName)
                .toList()
                .toString();
    }

    private String formatMediaStatus(WikiGameEntry wikiGameEntry) {
        var status = new StringBuilder();
        for (var mediaAssetType : MediaAssetType.values()) {
            status.append(wikiGameEntry.isMediaMissing(mediaAssetType) ? MEDIA_MISSING : MEDIA_EXISTS);
        }
        return status.toString();
    }

    private String formatMediaAssetOrder() {
        return java.util.Arrays.stream(MediaAssetType.values())
                .map(MediaAssetType::getDirectoryName)
                .toList()
                .toString();
    }

    private String formatReasons(List<String> reasons) {
        return String.join("；", reasons);
    }

    private String formatTags(Set<String> tags) {
        return tags.stream()
                .sorted(Comparator.naturalOrder())
                .toList()
                .toString();
    }
}

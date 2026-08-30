//package com.github.deathbit.retroboy.platform.impl;
//
//import com.github.deathbit.retroboy.domain.MediaCompletionRate;
//import com.github.deathbit.retroboy.domain.PlatformContext;
//import com.github.deathbit.retroboy.domain.WikiGameEntry;
//import com.github.deathbit.retroboy.enums.Area;
//import com.github.deathbit.retroboy.enums.MediaAssetType;
//import com.github.deathbit.retroboy.util.PathUtils;
//import com.github.deathbit.retroboy.util.ReleaseReportUtils;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.io.UncheckedIOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Comparator;
//import java.util.Locale;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@Component
//public class ReleaseReportHandler {
//
//    private static final DateTimeFormatter CREATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private static final String MEDIA_EXISTS = "●";
//    private static final String MEDIA_MISSING = "○";
//
//    public void handle(PlatformContext platformContext) {
//        var releaseReportPath = PathUtils.RELEASE_REPORT.get(platformContext);
//        var content = new StringBuilder();
//        ReleaseReportUtils.appendTitle(content);
//        appendDirectory(content);
//        ReleaseReportUtils.appendLine(content, "一、简介");
//        ReleaseReportUtils.appendIntroduction(content);
//        ReleaseReportUtils.appendLine(content, "二、合集特点");
//        ReleaseReportUtils.appendCollectionFeatures(content);
//        appendBasicInfo(content, platformContext);
//        ReleaseReportUtils.appendLine(content, "四、安装说明");
//        ReleaseReportUtils.appendInstallInstructions(content);
//        appendReleaseNotes(content, platformContext);
//        ReleaseReportUtils.appendLine(content, "六、目录说明");
//        ReleaseReportUtils.appendDirectoryInstructions(content);
//        ReleaseReportUtils.appendLine(content, "七、常见问题");
//        ReleaseReportUtils.appendFrequentlyAskedQuestions(content);
//        ReleaseReportUtils.appendLine(content, "八、捐助本项目");
//        ReleaseReportUtils.appendDonationInfo(content);
//        ReleaseReportUtils.appendLine(content, "九、关于作者");
//        ReleaseReportUtils.appendAuthorInfo(content, platformContext.getGlobalConfig().getAuthor());
//        ReleaseReportUtils.appendLine(content, "十、免责声明");
//        ReleaseReportUtils.appendDisclaimer(content);
//        ReleaseReportUtils.appendLine(content, "十一、未来规划");
//        ReleaseReportUtils.appendFuturePlan(content);
//        appendMediaMissingRates(content, platformContext);
//        appendGameList(content, platformContext);
//
//        try {
//            Files.createDirectories(releaseReportPath.getParent());
//            Files.writeString(releaseReportPath, content.toString(), StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            throw new UncheckedIOException("Failed to write release report: " + releaseReportPath, e);
//        }
//    }
//
//    private void appendDirectory(StringBuilder content) {
//        ReleaseReportUtils.appendLine(content, "目录");
//        ReleaseReportUtils.appendLine(content, "一、简介");
//        ReleaseReportUtils.appendLine(content, "二、合集特点");
//        ReleaseReportUtils.appendLine(content, "三、基础信息");
//        ReleaseReportUtils.appendLine(content, "四、安装说明");
//        ReleaseReportUtils.appendLine(content, "五、版本更新记录");
//        ReleaseReportUtils.appendLine(content, "六、目录说明");
//        ReleaseReportUtils.appendLine(content, "七、常见问题");
//        ReleaseReportUtils.appendLine(content, "八、捐助本项目");
//        ReleaseReportUtils.appendLine(content, "九、关于作者");
//        ReleaseReportUtils.appendLine(content, "十、免责声明");
//        ReleaseReportUtils.appendLine(content, "十一、未来规划");
//        ReleaseReportUtils.appendLine(content, "十二、媒体缺失率");
//        ReleaseReportUtils.appendLine(content, "十三、游戏清单");
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private void appendBasicInfo(StringBuilder content, PlatformContext platformContext) {
//        var areaGameCounts = buildAreaGameCounts(platformContext);
//        var platform = platformContext.getPlatform();
//        ReleaseReportUtils.appendLine(content, "三、基础信息");
//        ReleaseReportUtils.appendInfo(content, "平台名称", platform.name());
//        ReleaseReportUtils.appendInfo(content, "平台全称", platform.getSystemFullName());
//        ReleaseReportUtils.appendInfo(content, "所属公司", platform.getCompany());
//        ReleaseReportUtils.appendInfo(content, "默认核心", platformContext.getPlatformPackTaskConfig().getCore());
//        ReleaseReportUtils.appendInfo(content, "平台包版本", platformContext.getPlatformPackTaskConfig().getVersion());
//        ReleaseReportUtils.appendInfo(content, "ES-DE版本", platformContext.getGlobalConfig().getEsdeVersion());
//        ReleaseReportUtils.appendInfo(content, "RetroArch版本", platformContext.getGlobalConfig().getRaVersion());
//        ReleaseReportUtils.appendInfo(content, "创建时间", LocalDateTime.now().format(CREATE_TIME_FORMATTER));
//        ReleaseReportUtils.appendInfo(content, "支持地区", formatSupportedAreas(platformContext));
//        ReleaseReportUtils.appendInfo(content, "游戏总数", String.valueOf(areaGameCounts.values().stream().mapToInt(Integer::intValue).sum()));
//        appendAreaGameCounts(content, areaGameCounts);
//        ReleaseReportUtils.appendInfo(content, "基础包版本", platformContext.getAppConfig().getBasePackReleaseReportTaskConfig().getBasePackVersion());
//        ReleaseReportUtils.appendInfo(content, "百度网盘", platformContext.getGlobalConfig().getBaiduPan());
//        ReleaseReportUtils.appendInfo(content, "维基百科", platformContext.getPlatformPackTaskConfig().getWiki());
//        ReleaseReportUtils.appendInfo(content, "项目仓库", platformContext.getGlobalConfig().getRepo());
//        ReleaseReportUtils.appendInfo(content, "QQ群", platformContext.getGlobalConfig().getQqGroup());
//        ReleaseReportUtils.appendInfo(content, "反馈邮箱", platformContext.getGlobalConfig().getFeedbackEmail());
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private Map<Area, Integer> buildAreaGameCounts(PlatformContext platformContext) {
//        return platformContext.getAreaPassMap()
//                              .entrySet()
//                              .stream()
//                              .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
//    }
//
//    private void appendAreaGameCounts(StringBuilder content, Map<Area, Integer> areaGameCounts) {
//        Stream.of(Area.values())
//                .filter(areaGameCounts::containsKey)
//                .sorted(Comparator.comparingInt(Enum::ordinal))
//                .forEach(area -> ReleaseReportUtils.appendInfo(content,
//                        areaDisplayName(area) + "地区游戏总数",
//                        String.valueOf(areaGameCounts.get(area))));
//    }
//
//    private String formatSupportedAreas(PlatformContext platformContext) {
//        var passAreas = platformContext.getAreaPassMap()
//                                       .keySet()
//                                       .stream()
//                                       .toList();
//        return Stream.of(Area.values())
//                .filter(passAreas::contains)
//                .sorted(Comparator.comparingInt(Enum::ordinal))
//                .map(area -> platformContext.getPlatform().name() + "-" + area.name() + "（" + areaDisplayName(area) + "）")
//                .collect(Collectors.joining("、"));
//    }
//
//    private String areaDisplayName(Area area) {
//        return area.getChineseName();
//    }
//
//    private void appendReleaseNotes(StringBuilder content, PlatformContext platformContext) {
//        var releaseNotes = platformContext.getPlatformPackTaskConfig().getReleaseNotes();
//        ReleaseReportUtils.appendLine(content, "五、版本更新记录");
//        if (releaseNotes == null || releaseNotes.isEmpty()) {
//            ReleaseReportUtils.appendLine(content, "暂无版本更新记录");
//            ReleaseReportUtils.appendBlankLine(content);
//            return;
//        }
//
//        releaseNotes.forEach(releaseNote -> {
//            ReleaseReportUtils.appendLine(content, "[" + releaseNote.getVersion() + "] " + releaseNote.getDate());
//            releaseNote.getChanges().forEach(change -> ReleaseReportUtils.appendFeature(content, change));
//        });
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private void appendMediaMissingRates(StringBuilder content, PlatformContext platformContext) {
//        ReleaseReportUtils.appendLine(content, "十二、媒体缺失率");
//        var mediaCompletionRateMap = platformContext.getMediaCompletionRateMap();
//        Stream.of(Area.values())
//                .filter(area -> mediaCompletionRateMap != null && mediaCompletionRateMap.containsKey(area))
//                .sorted(Comparator.comparingInt(Enum::ordinal))
//                .forEach(area -> appendAreaMediaMissingRates(content, platformContext, area));
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private void appendAreaMediaMissingRates(StringBuilder content, PlatformContext platformContext, Area area) {
//        var mediaAssetRateMap = platformContext.getMediaCompletionRateMap().getOrDefault(area, Map.of());
//        ReleaseReportUtils.appendLine(content, "[" + platformContext.getPlatform().name() + "-" + area.name() + "] "
//                + areaDisplayName(area));
//        Stream.of(MediaAssetType.values())
//                .filter(mediaAssetRateMap::containsKey)
//                .forEach(mediaAssetType -> {
//                    var mediaCompletionRate = mediaAssetRateMap.get(mediaAssetType);
//                    ReleaseReportUtils.appendLine(content, mediaAssetType.getDirectoryName()
//                            + "：" + mediaCompletionRate.getCompletedCount()
//                            + "/" + mediaCompletionRate.getTotalCount()
//                            + "（" + formatCompletionRate(mediaCompletionRate) + "）");
//                });
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private void appendGameList(StringBuilder content, PlatformContext platformContext) {
//        ReleaseReportUtils.appendLine(content, "十三、游戏清单");
//        Stream.of(Area.values())
//                .filter(platformContext.getAreaWikiEntryMap()::containsKey)
//                .sorted(Comparator.comparingInt(Enum::ordinal))
//                .forEach(area -> appendAreaGameList(content, platformContext, area));
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private void appendAreaGameList(StringBuilder content, PlatformContext platformContext, Area area) {
//        var wikiGameEntries = platformContext.getAreaWikiEntryMap()
//                                             .getOrDefault(area, Map.of())
//                                             .values()
//                                             .stream()
//                                             .sorted(Comparator.comparing(WikiGameEntry::getWikiName))
//                                             .toList();
//        ReleaseReportUtils.appendLine(content, "[" + platformContext.getPlatform().name() + "-" + area.name() + "] "
//                + areaDisplayName(area) + "（" + wikiGameEntries.size() + " 个游戏）");
//        ReleaseReportUtils.appendLine(content, "序号 | 媒体缺失情况 | 维基百科条目 | 原始文件名称 | 修改后文件名称 | 最终名称 | 缺失媒体列表");
//        for (int i = 0; i < wikiGameEntries.size(); i++) {
//            var wikiGameEntry = wikiGameEntries.get(i);
//            ReleaseReportUtils.appendLine(content, String.format("%04d", i + 1)
//                    + " | " + formatMediaStatus(wikiGameEntry)
//                    + " | " + wikiGameEntry.getWikiName()
//                    + " | " + getOldName(wikiGameEntry)
//                    + " | " + getNewName(wikiGameEntry)
//                    + " | " + getFinalName(wikiGameEntry)
//                    + " | " + formatMissingMedia(wikiGameEntry));
//        }
//        ReleaseReportUtils.appendBlankLine(content);
//    }
//
//    private String getOldName(WikiGameEntry wikiGameEntry) {
//        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
//        if (areaRenameResult == null || areaRenameResult.getOldName() == null) {
//            return "";
//        }
//        return areaRenameResult.getOldName();
//    }
//
//    private String getNewName(WikiGameEntry wikiGameEntry) {
//        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
//        if (areaRenameResult == null || areaRenameResult.getNewName() == null) {
//            return "";
//        }
//        return areaRenameResult.getNewName();
//    }
//
//    private String getFinalName(WikiGameEntry wikiGameEntry) {
//        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
//        if (areaRenameResult == null || areaRenameResult.getFinalName() == null) {
//            return "";
//        }
//        return areaRenameResult.getFinalName();
//    }
//
//    private String formatMediaStatus(WikiGameEntry wikiGameEntry) {
//        var status = new StringBuilder();
//        for (var mediaAssetType : MediaAssetType.values()) {
//            status.append(wikiGameEntry.isMediaMissing(mediaAssetType) ? MEDIA_MISSING : MEDIA_EXISTS);
//        }
//        return status.toString();
//    }
//
//    private String formatMissingMedia(WikiGameEntry wikiGameEntry) {
//        return Stream.of(MediaAssetType.values())
//                .filter(wikiGameEntry::isMediaMissing)
//                .map(MediaAssetType::getDirectoryName)
//                .toList()
//                .toString();
//    }
//
//    private String formatCompletionRate(MediaCompletionRate mediaCompletionRate) {
//        var percentage = mediaCompletionRate.getCompletionRate() * 100;
//        if (percentage == Math.rint(percentage)) {
//            return String.format(Locale.ROOT, "%.0f%%", percentage);
//        }
//        return String.format(Locale.ROOT, "%.2f%%", percentage);
//    }
//}

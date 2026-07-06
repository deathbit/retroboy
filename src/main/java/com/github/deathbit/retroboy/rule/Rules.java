package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Rules {
    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    public static final Rule IS_LICENSED = Rule.named(
            "IS_LICENSED",
            (rc, fc) -> rc.getLicensed().contains(fc.getFullName()),
            (rc, fc) -> "DAT授权列表中不存在该游戏: " + fc.getFullName());
    public static final Rule IS_NOT_BAD = Rule.named(
            "IS_NOT_BAD",
            (rc, fc) -> !fc.getFullName().contains("[b]"),
            "文件名包含坏档标记 [b]");
    public static final Rule IS_NOT_HIT_GLOBAL_TAG_BLACKLIST = Rule.named(
            "IS_NOT_HIT_GLOBAL_TAG_BLACKLIST",
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getGlobalTagBlackList().contains(tag)),
            (rc, fc) -> "命中全局标签黑名单: " + matchingTags(fc.getTags(), rc.getGlobalTagBlackList()));
    public static final Rule IS_NOT_HIT_PLATFORM_TAG_BLACKLIST = Rule.named(
            "IS_NOT_HIT_PLATFORM_TAG_BLACKLIST",
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag)),
            (rc, fc) -> "命中平台标签黑名单: " + matchingTags(fc.getTags(), rc.getRuleConfig().getTagBlackList()));
    public static final Rule IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST = Rule.named(
            "IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST",
            (rc, fc) -> !rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName()),
            (rc, fc) -> "命中平台文件名黑名单: " + fc.getFileName());
    public static final Rule IS_JAPAN = Rule.named(
            "IS_JAPAN",
            (rc, fc) -> fc.getTagPart().contains("Japan"),
            "标签不包含 Japan");
    public static final Rule IS_USA = Rule.named(
            "IS_USA",
            (rc, fc) -> fc.getTagPart().contains("USA"),
            "标签不包含 USA");
    public static final Rule IS_EUROPE = Rule.named(
            "IS_EUROPE",
            (rc, fc) -> fc.getTagPart().contains("Europe"),
            "标签不包含 Europe");
    public static final Rule IS_AUSTRALIA = Rule.named(
            "IS_AUSTRALIA",
            (rc, fc) -> fc.getTagPart().contains("Australia"),
            "标签不包含 Australia");
    public static final Rule IS_GERMANY = Rule.named(
            "IS_GERMANY",
            (rc, fc) -> fc.getTagPart().contains("Germany"),
            "标签不包含 Germany");
    public static final Rule IS_SWEDEN = Rule.named(
            "IS_SWEDEN",
            (rc, fc) -> fc.getTagPart().contains("Sweden"),
            "标签不包含 Sweden");
    public static final Rule IS_FRANCE = Rule.named(
            "IS_FRANCE",
            (rc, fc) -> fc.getTagPart().contains("France"),
            "标签不包含 France");
    public static final Rule IS_SPAIN = Rule.named(
            "IS_SPAIN",
            (rc, fc) -> fc.getTagPart().contains("Spain"),
            "标签不包含 Spain");
    public static final Rule IS_PAL = IS_EUROPE.or(IS_AUSTRALIA).or(IS_GERMANY).or(IS_SWEDEN).or(IS_FRANCE).or(IS_SPAIN);
    public static final Rule IS_WORLD = Rule.named(
            "IS_WORLD",
            (rc, fc) -> fc.getTagPart().contains("World"),
            "标签不包含 World");
    public static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    public static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    public static final Rule IS_EUROPE_OR_WORLD = IS_PAL.or(IS_WORLD);
    public static final Rule IS_BASE = IS_LICENSED
            .and(IS_NOT_BAD)
            .and(IS_NOT_HIT_GLOBAL_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST);
    public static final Rule IS_JAPAN_BASE = IS_BASE.and(IS_JAPAN_OR_WORLD);
    public static final Rule IS_USA_BASE = IS_BASE.and(IS_USA_OR_WORLD);
    public static final Rule IS_EUROPE_BASE = IS_BASE.and(IS_EUROPE_OR_WORLD);

    public static Rule isNotHitAreaFileNameBlackList(AreaConfig areaConfig) {
        return Rule.named(
                "IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST",
                (rc, fc) -> areaConfig.getFileNameBlackList() == null
                        || !areaConfig.getFileNameBlackList().contains(fc.getFileName()),
                (rc, fc) -> "命中地区文件名黑名单: " + fc.getFileName());
    }

    public static void applyPreviousRevisionRule(RuleContext ruleContext) {
        var revisionRules = ruleContext.getFileContextMap().keySet().stream()
                .map(fileName -> new PreviousRevisionRule(fileName, previousRevision(fileName)))
                .filter(rule -> rule.previousFileName() != null)
                .toList();
        for (var revisionRule : revisionRules) {
            var previousRevision = revisionRule.previousFileName();
            var removedFileContext = ruleContext.getFileContextMap().remove(previousRevision);
            if (removedFileContext != null) {
                ruleContext.getSkippedFileReasonMap().put(previousRevision, "存在新版修订，已被替代: " + revisionRule.fileName());
            }
        }
    }

    public static void preferEuropeVersionForEuropeArea(RuleContext ruleContext) {
        var europeFinalFiles = ruleContext.getAreaFinalMap().get(Area.EUR);
        var europeRuleResultMap = ruleContext.getAreaRuleResultMap().get(Area.EUR);
        if (europeFinalFiles == null || europeRuleResultMap == null || europeFinalFiles.isEmpty()) {
            return;
        }

        var passedFileContextMap = new LinkedHashMap<String, List<FileContext>>();
        for (var fileName : europeFinalFiles) {
            var fileContext = ruleContext.getFileContextMap().get(fileName);
            if (fileContext != null) {
                passedFileContextMap.computeIfAbsent(fileContext.getNamePart(), ignored -> new ArrayList<>()).add(fileContext);
            }
        }

        var filesToRemove = new ArrayList<String>();
        for (var fileContexts : passedFileContextMap.values()) {
            var europeVersions = fileContexts.stream()
                    .filter(Rules::isEuropeVersion)
                    .map(FileContext::getFileName)
                    .toList();
            if (europeVersions.isEmpty()) {
                continue;
            }

            for (var fileContext : fileContexts) {
                if (!isEuropeVersion(fileContext)) {
                    filesToRemove.add(fileContext.getFileName());
                    europeRuleResultMap.put(fileContext.getFileName(), RuleResult.fail(
                            "PREFER_EUROPE_VERSION",
                            "存在同名 Europe 版本，已排除地区版本，保留: " + String.join(", ", europeVersions)));
                }
            }
        }

        europeFinalFiles.removeAll(filesToRemove);
    }

    private static boolean isEuropeVersion(FileContext fileContext) {
        return fileContext.getTags().contains("Europe");
    }

    private static String previousRevision(String filename) {
        var matcher = REV_TAG.matcher(filename);
        if (!matcher.find()) {
            return null;
        }

        var revision = Integer.parseInt(matcher.group(1));
        if (revision == 1) {
            return filename.substring(0, matcher.start())
                    .concat(filename.substring(matcher.end()))
                    .replaceAll("\\s+\\.", ".")
                    .replaceAll("\\s{2,}", " ")
                    .trim();
        }
        return filename.substring(0, matcher.start())
                .concat("(Rev " + (revision - 1) + ")")
                .concat(filename.substring(matcher.end()));
    }

    private static String matchingTags(Set<String> tags, Set<String> blacklist) {
        return tags.stream()
                .filter(blacklist::contains)
                .collect(Collectors.joining(", "));
    }

    private record PreviousRevisionRule(String fileName, String previousFileName) {
    }
}

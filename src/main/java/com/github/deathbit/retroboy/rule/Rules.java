package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Rules {
    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    private static final Rule IS_LICENSED = Rule.named(
            "IS_LICENSED",
            (rc, fc) -> rc.getLicensed().contains(fc.getFullName()),
            (rc, fc) -> "DAT授权列表中不存在该游戏: " + fc.getFullName());
    private static final Rule IS_NOT_BAD = Rule.named(
            "IS_NOT_BAD",
            (rc, fc) -> !fc.getFullName().contains("[b]"),
            "文件名包含坏档标记 [b]");
    private static final Rule IS_NOT_HIT_GLOBAL_TAG_BLACKLIST = Rule.named(
            "IS_NOT_HIT_GLOBAL_TAG_BLACKLIST",
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getGlobalTagBlackList().contains(tag)),
            (rc, fc) -> "命中全局标签黑名单: " + matchingTags(fc.getTags(), rc.getGlobalTagBlackList()));
    private static final Rule IS_NOT_HIT_PLATFORM_TAG_BLACKLIST = Rule.named(
            "IS_NOT_HIT_PLATFORM_TAG_BLACKLIST",
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag)),
            (rc, fc) -> "命中平台标签黑名单: " + matchingTags(fc.getTags(), rc.getRuleConfig().getTagBlackList()));
    private static final Rule IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST = Rule.named(
            "IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST",
            (rc, fc) -> !rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName()),
            (rc, fc) -> "命中平台文件名黑名单: " + fc.getFileName());
    private static final Rule IS_NOT_PREVIOUS_REVISION = Rule.named(
            "IS_NOT_PREVIOUS_REVISION",
            (rc, fc) -> newerRevisionFileName(rc, fc.getFileName()).isEmpty(),
            (rc, fc) -> "存在新版修订，已被替代: " + newerRevisionFileName(rc, fc.getFileName()).orElse(""));
    private static final Rule IS_JAPAN = Rule.named(
            "IS_JAPAN",
            (rc, fc) -> fc.getTagPart().contains("Japan"),
            "标签不包含 Japan");
    private static final Rule IS_USA = Rule.named(
            "IS_USA",
            (rc, fc) -> fc.getTagPart().contains("USA"),
            "标签不包含 USA");
    private static final Rule IS_EUROPE = Rule.named(
            "IS_EUROPE",
            (rc, fc) -> fc.getTagPart().contains("Europe"),
            "标签不包含 Europe");
    private static final Rule IS_AUSTRALIA = Rule.named(
            "IS_AUSTRALIA",
            (rc, fc) -> fc.getTagPart().contains("Australia"),
            "标签不包含 Australia");
    private static final Rule IS_GERMANY = Rule.named(
            "IS_GERMANY",
            (rc, fc) -> fc.getTagPart().contains("Germany"),
            "标签不包含 Germany");
    private static final Rule IS_SWEDEN = Rule.named(
            "IS_SWEDEN",
            (rc, fc) -> fc.getTagPart().contains("Sweden"),
            "标签不包含 Sweden");
    private static final Rule IS_FRANCE = Rule.named(
            "IS_FRANCE",
            (rc, fc) -> fc.getTagPart().contains("France"),
            "标签不包含 France");
    private static final Rule IS_SPAIN = Rule.named(
            "IS_SPAIN",
            (rc, fc) -> fc.getTagPart().contains("Spain"),
            "标签不包含 Spain");
    private static final Rule IS_PAL = IS_EUROPE.or(IS_AUSTRALIA).or(IS_GERMANY).or(IS_SWEDEN).or(IS_FRANCE).or(IS_SPAIN);
    private static final Rule IS_WORLD = Rule.named(
            "IS_WORLD",
            (rc, fc) -> fc.getTagPart().contains("World"),
            "标签不包含 World");
    private static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    private static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    private static final Rule IS_EUROPE_OR_WORLD = IS_PAL.or(IS_WORLD);
    private static final Rule IS_BASE = IS_LICENSED
            .and(IS_NOT_BAD)
            .and(IS_NOT_HIT_GLOBAL_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST)
            .and(IS_NOT_PREVIOUS_REVISION);
    public static final Rule IS_JAPAN_BASE = areaBase(Area.JPN, IS_JAPAN_OR_WORLD);
    public static final Rule IS_USA_BASE = areaBase(Area.USA, IS_USA_OR_WORLD);
    private static final Rule IS_EUROPE_BASE_WITHOUT_PREFERENCE = areaBase(Area.EUR, IS_EUROPE_OR_WORLD);
    public static final Rule IS_EUROPE_BASE = IS_EUROPE_BASE_WITHOUT_PREFERENCE.and(preferEuropeVersion());

    private static Rule areaBase(Area area, Rule areaRule) {
        return IS_BASE.and(areaRule).and(isNotHitAreaFileNameBlackList(area));
    }

    private static Rule isNotHitAreaFileNameBlackList(Area area) {
        return Rule.named(
                "IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST",
                (rc, fc) -> areaConfig(rc, area)
                        .map(AreaConfig::getFileNameBlackList)
                        .map(fileNameBlackList -> !fileNameBlackList.contains(fc.getFileName()))
                        .orElse(true),
                (rc, fc) -> "命中地区文件名黑名单: " + fc.getFileName());
    }

    private static Rule preferEuropeVersion() {
        return Rule.named(
                "PREFER_EUROPE_VERSION",
                (rc, fc) -> !IS_EUROPE_BASE_WITHOUT_PREFERENCE.pass(rc, fc)
                        || isEuropeVersion(fc)
                        || preferredEuropeVersionFileNames(rc, fc).isEmpty(),
                (rc, fc) -> "存在同名 Europe 版本，已排除地区版本，保留: "
                        + String.join(", ", preferredEuropeVersionFileNames(rc, fc)));
    }

    private static String previousRevision(String filename) {
        var matcher = REV_TAG.matcher(filename);
        if (!matcher.find()) {
            return null;
        }

        try {
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
        } catch (NumberFormatException e) {
            // Leave revision tags that cannot be parsed as an integer untouched.
            return null;
        }
    }

    private static String matchingTags(Set<String> tags, Set<String> blacklist) {
        return tags.stream()
                .filter(blacklist::contains)
                .collect(Collectors.joining(", "));
    }

    private static Optional<AreaConfig> areaConfig(RuleContext ruleContext, Area area) {
        if (ruleContext.getRuleConfig().getTargetAreaConfigs() == null) {
            return Optional.empty();
        }

        return ruleContext.getRuleConfig().getTargetAreaConfigs().stream()
                .filter(areaConfig -> area == areaConfig.getArea())
                .findFirst();
    }

    private static Optional<String> newerRevisionFileName(RuleContext ruleContext, String fileName) {
        if (ruleContext.getFileContextMap() == null) {
            return Optional.empty();
        }

        return ruleContext.getFileContextMap().keySet().stream()
                .filter(candidateFileName -> fileName.equals(previousRevision(candidateFileName)))
                .findFirst();
    }

    private static List<String> preferredEuropeVersionFileNames(RuleContext ruleContext, FileContext fileContext) {
        if (ruleContext.getFileContextMap() == null) {
            return List.of();
        }

        return ruleContext.getFileContextMap().values().stream()
                .filter(other -> other.getNamePart().equals(fileContext.getNamePart()))
                .filter(Rules::isEuropeVersion)
                .filter(other -> IS_EUROPE_BASE_WITHOUT_PREFERENCE.pass(ruleContext, other))
                .map(FileContext::getFileName)
                .toList();
    }

    private static boolean isEuropeVersion(FileContext fileContext) {
        return fileContext.getTags().stream().anyMatch("Europe"::equals);
    }
}

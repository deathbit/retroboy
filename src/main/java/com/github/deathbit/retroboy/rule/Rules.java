package com.github.deathbit.retroboy.rule;

import java.util.Set;
import java.util.stream.Collectors;

public class Rules {
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

    private static String matchingTags(Set<String> tags, Set<String> blacklist) {
        return tags.stream()
                .filter(blacklist::contains)
                .collect(Collectors.joining(", "));
    }
}

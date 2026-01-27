package com.github.deathbit.retroboy.rule;

public class Rules {
    public static final Rule IS_LICENSED = (rc, fc) -> rc.getLicensed().contains(fc.getFullName());
    public static final Rule IS_NOT_BAD = (rc, fc) -> !fc.getFullName().contains("[b]");
    public static final Rule IS_NOT_BIOS = (rc, fc) -> !fc.getFullName().contains("[BIOS]");
    public static final Rule IS_NOT_HIT_GLOBAL_TAG_BLACKLIST = (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getGlobalTagBlackList().contains(tag));
    public static final Rule IS_NOT_HIT_PLATFORM_TAG_BLACKLIST = (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag));
    public static final Rule IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST = (rc, fc) -> !rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName());
    public static final Rule IS_JAPAN = (rc, fc) -> fc.getTagPart().contains("Japan");
    public static final Rule IS_USA = (rc, fc) -> fc.getTagPart().contains("USA");
    public static final Rule IS_EUROPE = (rc, fc) -> fc.getTagPart().contains("Europe");
    public static final Rule IS_WORLD = (rc, fc) -> fc.getTagPart().contains("World");
    public static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    public static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    public static final Rule IS_EUROPE_OR_WORLD = IS_EUROPE.or(IS_WORLD);
    public static final Rule IS_BASE = IS_LICENSED
        .and(IS_NOT_BAD)
        .and(IS_NOT_BIOS)
        .and(IS_NOT_HIT_GLOBAL_TAG_BLACKLIST)
        .and(IS_NOT_HIT_PLATFORM_TAG_BLACKLIST)
        .and(IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST);
    public static final Rule IS_JAPAN_BASE = IS_BASE.and(IS_JAPAN_OR_WORLD);
    public static final Rule IS_USA_BASE = IS_BASE.and(IS_USA_OR_WORLD);
    public static final Rule IS_EUROPE_BASE = IS_BASE.and(IS_EUROPE_OR_WORLD);
}

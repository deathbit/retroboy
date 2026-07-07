package com.github.deathbit.retroboy.rule;

public class Rules {
    public static final Rule IS_LICENSED = (rc, fc) -> rc.getLicensed().contains(fc.getFullName());
    public static final Rule IS_BAD = (rc, fc) -> fc.getFullName().contains("[b]");
    public static final Rule IS_HIT_GLOBAL_TAG_BLACKLIST = (rc, fc) -> fc.getTags().stream().anyMatch(tag -> rc.getGlobalTagBlackList().contains(tag));
    public static final Rule IS_HIT_PLATFORM_TAG_BLACKLIST = (rc, fc) -> fc.getTags().stream().anyMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag));
    public static final Rule IS_HIT_PLATFORM_FILE_NAME_BLACKLIST = (rc, fc) -> rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName());
    public static final Rule IS_PREVIOUS_REVISION = new PreviousRevisionRule();
    public static final Rule IS_JAPAN = (rc, fc) -> fc.getTagPart().contains("Japan");
    public static final Rule IS_USA = (rc, fc) -> fc.getTagPart().contains("USA");
    public static final Rule IS_EUROPE = (rc, fc) -> fc.getTagPart().contains("Europe");
    public static final Rule IS_AUSTRALIA = (rc, fc) -> fc.getTagPart().contains("Australia");
    public static final Rule IS_GERMANY = (rc, fc) -> fc.getTagPart().contains("Germany");
    public static final Rule IS_SWEDEN = (rc, fc) -> fc.getTagPart().contains("Sweden");
    public static final Rule IS_FRANCE = (rc, fc) -> fc.getTagPart().contains("France");
    public static final Rule IS_SPAIN = (rc, fc) -> fc.getTagPart().contains("Spain");
    public static final Rule IS_PAL = IS_EUROPE.or(IS_AUSTRALIA).or(IS_GERMANY).or(IS_SWEDEN).or(IS_FRANCE).or(IS_SPAIN);
    public static final Rule IS_WORLD = (rc, fc) -> fc.getTagPart().contains("World");
    public static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    public static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    public static final Rule IS_EUROPE_OR_WORLD = IS_PAL.or(IS_WORLD);
    public static final Rule IS_HIT_AREA_FILE_NAME_BLACKLIST = new AreaFileNameBlacklistRule();
    public static final Rule IS_BASE_WITHOUT_PREFERENCE = IS_LICENSED.and(IS_BAD.not()).and(IS_HIT_GLOBAL_TAG_BLACKLIST.not()).and(IS_HIT_PLATFORM_TAG_BLACKLIST.not()).and(IS_HIT_PLATFORM_FILE_NAME_BLACKLIST.not()).and(IS_PREVIOUS_REVISION.not()).and(IS_HIT_AREA_FILE_NAME_BLACKLIST.not());
    public static final Rule PREFER_EUROPE_VERSION = new PreferEuropeVersionRule(IS_BASE_WITHOUT_PREFERENCE.and(IS_EUROPE_OR_WORLD));
    public static final Rule IS_BASE = IS_BASE_WITHOUT_PREFERENCE.and(PREFER_EUROPE_VERSION);
    public static final Rule IS_JAPAN_BASE = IS_BASE.and(IS_JAPAN_OR_WORLD);
    public static final Rule IS_USA_BASE = IS_BASE.and(IS_USA_OR_WORLD);
    public static final Rule IS_EUROPE_BASE = IS_BASE.and(IS_EUROPE_OR_WORLD);
}

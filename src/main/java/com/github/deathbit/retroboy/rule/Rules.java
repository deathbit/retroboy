package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class Rules {
    public static final Rule IS_LICENSED = rule((rc, fc) -> rc.getLicensed().contains(fc.getFullName()), "DAT授权清单中不存在");
    public static final Rule IS_NOT_BAD = rule((rc, fc) -> !fc.getFullName().contains("[b]"), "文件名包含坏档标记 [b]");
    public static final Rule IS_NOT_HIT_GLOBAL_TAG_BLACKLIST = rule(
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getGlobalTagBlackList().contains(tag)),
            (rc, fc) -> "命中全局标签黑名单: " + matchedGlobalTag(rc, fc));
    public static final Rule IS_NOT_HIT_PLATFORM_TAG_BLACKLIST = rule(
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag)),
            (rc, fc) -> "命中平台标签黑名单: " + matchedPlatformTag(rc, fc));
    public static final Rule IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST = rule(
            (rc, fc) -> !rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName()),
            "命中平台文件名黑名单");
    public static final Rule IS_NOT_PREVIOUS_REVISION = new PreviousRevisionRule();
    public static final Rule IS_JAPAN = rule((rc, fc) -> fc.getTagPart().contains("Japan"), "不属于 Japan 地区");
    public static final Rule IS_USA = rule((rc, fc) -> fc.getTagPart().contains("USA"), "不属于 USA 地区");
    public static final Rule IS_EUROPE = rule((rc, fc) -> fc.getTagPart().contains("Europe"), "不属于 Europe 地区");
    public static final Rule IS_AUSTRALIA = rule((rc, fc) -> fc.getTagPart().contains("Australia"), "不属于 Australia 地区");
    public static final Rule IS_GERMANY = rule((rc, fc) -> fc.getTagPart().contains("Germany"), "不属于 Germany 地区");
    public static final Rule IS_SWEDEN = rule((rc, fc) -> fc.getTagPart().contains("Sweden"), "不属于 Sweden 地区");
    public static final Rule IS_FRANCE = rule((rc, fc) -> fc.getTagPart().contains("France"), "不属于 France 地区");
    public static final Rule IS_SPAIN = rule((rc, fc) -> fc.getTagPart().contains("Spain"), "不属于 Spain 地区");
    public static final Rule IS_PAL = IS_EUROPE.or(IS_AUSTRALIA).or(IS_GERMANY).or(IS_SWEDEN).or(IS_FRANCE).or(IS_SPAIN);
    public static final Rule IS_WORLD = rule((rc, fc) -> fc.getTagPart().contains("World"), "不属于 World 版本");
    public static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    public static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    public static final Rule IS_EUROPE_OR_WORLD = IS_PAL.or(IS_WORLD);
    public static final Rule IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST = new AreaFileNameBlacklistRule();
    public static final Rule IS_BASE_WITHOUT_PREFERENCE = IS_LICENSED
            .and(IS_NOT_BAD)
            .and(IS_NOT_HIT_GLOBAL_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST)
            .and(IS_NOT_PREVIOUS_REVISION)
            .and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    public static final Rule PREFER_EUROPE_VERSION = new PreferEuropeVersionRule(IS_BASE_WITHOUT_PREFERENCE.and(IS_EUROPE_OR_WORLD));
    public static final Rule IS_BASE = IS_BASE_WITHOUT_PREFERENCE.and(PREFER_EUROPE_VERSION);
    public static final Rule IS_JAPAN_BASE = IS_BASE.and(IS_JAPAN_OR_WORLD);
    public static final Rule IS_USA_BASE = IS_BASE.and(IS_USA_OR_WORLD);
    public static final Rule IS_EUROPE_BASE = IS_BASE.and(IS_EUROPE_OR_WORLD);

    private static Rule rule(BiPredicate<RuleContext, FileContext> predicate, String failureReason) {
        return (ruleContext, fileContext) -> predicate.test(ruleContext, fileContext)
                ? RuleResult.success()
                : RuleResult.failed(failureReason);
    }

    private static Rule rule(BiPredicate<RuleContext, FileContext> predicate,
                             BiFunction<RuleContext, FileContext, String> failureReason) {
        return (ruleContext, fileContext) -> predicate.test(ruleContext, fileContext)
                ? RuleResult.success()
                : RuleResult.failed(failureReason.apply(ruleContext, fileContext));
    }

    private static String matchedGlobalTag(RuleContext ruleContext, FileContext fileContext) {
        return fileContext.getTags().stream()
                .filter(tag -> ruleContext.getGlobalTagBlackList().contains(tag))
                .findFirst()
                .orElse("");
    }

    private static String matchedPlatformTag(RuleContext ruleContext, FileContext fileContext) {
        return fileContext.getTags().stream()
                .filter(tag -> ruleContext.getRuleConfig().getTagBlackList().contains(tag))
                .findFirst()
                .orElse("");
    }
}

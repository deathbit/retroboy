package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Rules {
    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    private static final Rule IS_LICENSED = (rc, fc) -> rc.getLicensed().contains(fc.getFullName());
    private static final Rule IS_NOT_BAD = (rc, fc) -> !fc.getFullName().contains("[b]");
    private static final Rule IS_NOT_HIT_GLOBAL_TAG_BLACKLIST =
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getGlobalTagBlackList().contains(tag));
    private static final Rule IS_NOT_HIT_PLATFORM_TAG_BLACKLIST =
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag));
    private static final Rule IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST =
            (rc, fc) -> !rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName());
    private static final Rule IS_NOT_PREVIOUS_REVISION =
            (rc, fc) -> newerRevisionFileName(rc, fc.getFileName()).isEmpty();
    private static final Rule IS_JAPAN = (rc, fc) -> fc.getTagPart().contains("Japan");
    private static final Rule IS_USA = (rc, fc) -> fc.getTagPart().contains("USA");
    private static final Rule IS_EUROPE = (rc, fc) -> fc.getTagPart().contains("Europe");
    private static final Rule IS_AUSTRALIA = (rc, fc) -> fc.getTagPart().contains("Australia");
    private static final Rule IS_GERMANY = (rc, fc) -> fc.getTagPart().contains("Germany");
    private static final Rule IS_SWEDEN = (rc, fc) -> fc.getTagPart().contains("Sweden");
    private static final Rule IS_FRANCE = (rc, fc) -> fc.getTagPart().contains("France");
    private static final Rule IS_SPAIN = (rc, fc) -> fc.getTagPart().contains("Spain");
    private static final Rule IS_PAL = IS_EUROPE.or(IS_AUSTRALIA).or(IS_GERMANY).or(IS_SWEDEN).or(IS_FRANCE).or(IS_SPAIN);
    private static final Rule IS_WORLD = (rc, fc) -> fc.getTagPart().contains("World");
    private static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    private static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    private static final Rule IS_EUROPE_OR_WORLD = IS_PAL.or(IS_WORLD);
    private static final Rule IS_BASE = IS_LICENSED
            .and(IS_NOT_BAD)
            .and(IS_NOT_HIT_GLOBAL_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST)
            .and(IS_NOT_PREVIOUS_REVISION);
    private static final Rule IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST = (rc, fc) -> areaConfig(rc)
            .map(AreaConfig::getFileNameBlackList)
            .map(fileNameBlackList -> !fileNameBlackList.contains(fc.getFileName()))
            .orElse(true);
    public static final Rule IS_JAPAN_BASE = IS_BASE.and(IS_JAPAN_OR_WORLD).and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    public static final Rule IS_USA_BASE = IS_BASE.and(IS_USA_OR_WORLD).and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    private static final Rule IS_EUROPE_BASE_WITHOUT_PREFERENCE = IS_BASE.and(IS_EUROPE_OR_WORLD).and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    private static final Rule PREFER_EUROPE_VERSION = (rc, fc) -> !IS_EUROPE_BASE_WITHOUT_PREFERENCE.pass(rc, fc)
            || isEuropeVersion(fc)
            || preferredEuropeVersionFileNames(rc, fc).isEmpty();
    public static final Rule IS_EUROPE_BASE = IS_EUROPE_BASE_WITHOUT_PREFERENCE.and(PREFER_EUROPE_VERSION);
    private static final Map<String, Rule> BASE_RULES = orderedRules(
            Map.entry("IS_LICENSED", IS_LICENSED),
            Map.entry("IS_NOT_BAD", IS_NOT_BAD),
            Map.entry("IS_NOT_HIT_GLOBAL_TAG_BLACKLIST", IS_NOT_HIT_GLOBAL_TAG_BLACKLIST),
            Map.entry("IS_NOT_HIT_PLATFORM_TAG_BLACKLIST", IS_NOT_HIT_PLATFORM_TAG_BLACKLIST),
            Map.entry("IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST", IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST),
            Map.entry("IS_NOT_PREVIOUS_REVISION", IS_NOT_PREVIOUS_REVISION));
    private static final Map<String, Rule> PAL_RULES = orderedRules(
            Map.entry("IS_EUROPE", IS_EUROPE),
            Map.entry("IS_AUSTRALIA", IS_AUSTRALIA),
            Map.entry("IS_GERMANY", IS_GERMANY),
            Map.entry("IS_SWEDEN", IS_SWEDEN),
            Map.entry("IS_FRANCE", IS_FRANCE),
            Map.entry("IS_SPAIN", IS_SPAIN));

    public static List<String> failedRuleNames(Area area, RuleContext ruleContext, FileContext fileContext) {
        return switch (area) {
            case JPN -> failedJapanRuleNames(ruleContext, fileContext);
            case USA -> failedUsaRuleNames(ruleContext, fileContext);
            case EUR -> failedEuropeRuleNames(ruleContext, fileContext);
        };
    }

    private static List<String> failedJapanRuleNames(RuleContext ruleContext, FileContext fileContext) {
        var baseFailures = failedBaseRuleNames(ruleContext, fileContext);
        if (!baseFailures.isEmpty()) {
            return baseFailures;
        }

        var areaFailures = failedJapanOrWorldRuleNames(ruleContext, fileContext);
        if (!areaFailures.isEmpty()) {
            return areaFailures;
        }

        return failedRuleNames(ruleContext, fileContext, "IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST", IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    }

    private static List<String> failedUsaRuleNames(RuleContext ruleContext, FileContext fileContext) {
        var baseFailures = failedBaseRuleNames(ruleContext, fileContext);
        if (!baseFailures.isEmpty()) {
            return baseFailures;
        }

        var areaFailures = failedUsaOrWorldRuleNames(ruleContext, fileContext);
        if (!areaFailures.isEmpty()) {
            return areaFailures;
        }

        return failedRuleNames(ruleContext, fileContext, "IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST", IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    }

    private static List<String> failedEuropeRuleNames(RuleContext ruleContext, FileContext fileContext) {
        var baseFailures = failedBaseRuleNames(ruleContext, fileContext);
        if (!baseFailures.isEmpty()) {
            return baseFailures;
        }

        var areaFailures = failedEuropeOrWorldRuleNames(ruleContext, fileContext);
        if (!areaFailures.isEmpty()) {
            return areaFailures;
        }

        var areaFileNameFailures =
                failedRuleNames(ruleContext, fileContext, "IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST", IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
        if (!areaFileNameFailures.isEmpty()) {
            return areaFileNameFailures;
        }

        return failedRuleNames(ruleContext, fileContext, "PREFER_EUROPE_VERSION", PREFER_EUROPE_VERSION);
    }

    private static List<String> failedBaseRuleNames(RuleContext ruleContext, FileContext fileContext) {
        return firstFailedRuleNames(ruleContext, fileContext, BASE_RULES);
    }

    private static List<String> failedJapanOrWorldRuleNames(RuleContext ruleContext, FileContext fileContext) {
        if (IS_JAPAN_OR_WORLD.pass(ruleContext, fileContext)) {
            return List.of();
        }

        return failedRuleNames(
                ruleContext,
                fileContext,
                orderedRules(Map.entry("IS_JAPAN", IS_JAPAN), Map.entry("IS_WORLD", IS_WORLD)));
    }

    private static List<String> failedUsaOrWorldRuleNames(RuleContext ruleContext, FileContext fileContext) {
        if (IS_USA_OR_WORLD.pass(ruleContext, fileContext)) {
            return List.of();
        }

        return failedRuleNames(
                ruleContext,
                fileContext,
                orderedRules(Map.entry("IS_USA", IS_USA), Map.entry("IS_WORLD", IS_WORLD)));
    }

    private static List<String> failedEuropeOrWorldRuleNames(RuleContext ruleContext, FileContext fileContext) {
        if (IS_EUROPE_OR_WORLD.pass(ruleContext, fileContext)) {
            return List.of();
        }

        var palFailures = failedRuleNames(ruleContext, fileContext, PAL_RULES);
        var worldFailures = failedRuleNames(ruleContext, fileContext, "IS_WORLD", IS_WORLD);
        return Stream.concat(palFailures.stream(), worldFailures.stream()).toList();
    }

    private static List<String> firstFailedRuleNames(
            RuleContext ruleContext,
            FileContext fileContext,
            Map<String, Rule> rules) {
        return rules.entrySet().stream()
                .filter(entry -> !entry.getValue().pass(ruleContext, fileContext))
                .map(Map.Entry::getKey)
                .findFirst()
                .map(List::of)
                .orElseGet(List::of);
    }

    private static List<String> failedRuleNames(
            RuleContext ruleContext,
            FileContext fileContext,
            Map<String, Rule> rules) {
        return rules.entrySet().stream()
                .filter(entry -> !entry.getValue().pass(ruleContext, fileContext))
                .map(Map.Entry::getKey)
                .toList();
    }

    private static List<String> failedRuleNames(RuleContext ruleContext, FileContext fileContext, String name, Rule rule) {
        return rule.pass(ruleContext, fileContext) ? List.of() : List.of(name);
    }

    @SafeVarargs
    private static Map<String, Rule> orderedRules(Map.Entry<String, Rule>... entries) {
        var rules = new LinkedHashMap<String, Rule>();
        for (var entry : entries) {
            rules.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(rules);
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

    private static Optional<AreaConfig> areaConfig(RuleContext ruleContext) {
        if (ruleContext.getRuleConfig().getTargetAreaConfigs() == null) {
            return Optional.empty();
        }

        return ruleContext.getRuleConfig().getTargetAreaConfigs().stream()
                .filter(areaConfig -> ruleContext.getCurrentArea() == areaConfig.getArea())
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
        return fileContext.getTags().contains("Europe");
    }
}

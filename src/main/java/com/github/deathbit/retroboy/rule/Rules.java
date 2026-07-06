package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Rules {
    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    private static final RuleNode IS_LICENSED = rule(
            "IS_LICENSED",
            (rc, fc) -> rc.getLicensed().contains(fc.getFullName()));
    private static final RuleNode IS_NOT_BAD = rule(
            "IS_NOT_BAD",
            (rc, fc) -> !fc.getFullName().contains("[b]"));
    private static final RuleNode IS_NOT_HIT_GLOBAL_TAG_BLACKLIST = rule(
            "IS_NOT_HIT_GLOBAL_TAG_BLACKLIST",
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getGlobalTagBlackList().contains(tag)));
    private static final RuleNode IS_NOT_HIT_PLATFORM_TAG_BLACKLIST = rule(
            "IS_NOT_HIT_PLATFORM_TAG_BLACKLIST",
            (rc, fc) -> fc.getTags().stream().noneMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag)));
    private static final RuleNode IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST = rule(
            "IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST",
            (rc, fc) -> !rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName()));
    private static final RuleNode IS_NOT_PREVIOUS_REVISION = rule(
            "IS_NOT_PREVIOUS_REVISION",
            (rc, fc) -> newerRevisionFileName(rc, fc.getFileName()).isEmpty());
    private static final RuleNode IS_JAPAN = rule(
            "IS_JAPAN",
            (rc, fc) -> fc.getTagPart().contains("Japan"));
    private static final RuleNode IS_USA = rule(
            "IS_USA",
            (rc, fc) -> fc.getTagPart().contains("USA"));
    private static final RuleNode IS_EUROPE = rule(
            "IS_EUROPE",
            (rc, fc) -> fc.getTagPart().contains("Europe"));
    private static final RuleNode IS_AUSTRALIA = rule(
            "IS_AUSTRALIA",
            (rc, fc) -> fc.getTagPart().contains("Australia"));
    private static final RuleNode IS_GERMANY = rule(
            "IS_GERMANY",
            (rc, fc) -> fc.getTagPart().contains("Germany"));
    private static final RuleNode IS_SWEDEN = rule(
            "IS_SWEDEN",
            (rc, fc) -> fc.getTagPart().contains("Sweden"));
    private static final RuleNode IS_FRANCE = rule(
            "IS_FRANCE",
            (rc, fc) -> fc.getTagPart().contains("France"));
    private static final RuleNode IS_SPAIN = rule(
            "IS_SPAIN",
            (rc, fc) -> fc.getTagPart().contains("Spain"));
    private static final RuleNode IS_PAL = IS_EUROPE.or(IS_AUSTRALIA).or(IS_GERMANY).or(IS_SWEDEN).or(IS_FRANCE).or(IS_SPAIN);
    private static final RuleNode IS_WORLD = rule(
            "IS_WORLD",
            (rc, fc) -> fc.getTagPart().contains("World"));
    private static final RuleNode IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    private static final RuleNode IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    private static final RuleNode IS_EUROPE_OR_WORLD = IS_PAL.or(IS_WORLD);
    private static final RuleNode IS_BASE = IS_LICENSED
            .and(IS_NOT_BAD)
            .and(IS_NOT_HIT_GLOBAL_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_TAG_BLACKLIST)
            .and(IS_NOT_HIT_PLATFORM_FILE_NAME_BLACKLIST)
            .and(IS_NOT_PREVIOUS_REVISION);
    private static final RuleNode IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST = rule(
            "IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST",
            (rc, fc) -> areaConfig(rc)
                    .map(AreaConfig::getFileNameBlackList)
                    .map(fileNameBlackList -> !fileNameBlackList.contains(fc.getFileName()))
                    .orElse(true));
    private static final RuleNode IS_JAPAN_BASE_NODE = IS_BASE.and(IS_JAPAN_OR_WORLD).and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    private static final RuleNode IS_USA_BASE_NODE = IS_BASE.and(IS_USA_OR_WORLD).and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    private static final RuleNode IS_EUROPE_BASE_WITHOUT_PREFERENCE = IS_BASE.and(IS_EUROPE_OR_WORLD).and(IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST);
    private static final RuleNode PREFER_EUROPE_VERSION = rule(
            "PREFER_EUROPE_VERSION",
            (rc, fc) -> !IS_EUROPE_BASE_WITHOUT_PREFERENCE.pass(rc, fc)
                    || isEuropeVersion(fc)
                    || preferredEuropeVersionFileNames(rc, fc).isEmpty());
    private static final RuleNode IS_EUROPE_BASE_NODE = IS_EUROPE_BASE_WITHOUT_PREFERENCE.and(PREFER_EUROPE_VERSION);

    public static final Rule IS_JAPAN_BASE = IS_JAPAN_BASE_NODE.asRule();
    public static final Rule IS_USA_BASE = IS_USA_BASE_NODE.asRule();
    public static final Rule IS_EUROPE_BASE = IS_EUROPE_BASE_NODE.asRule();

    public static List<String> failedRuleNames(Area area, RuleContext ruleContext, FileContext fileContext) {
        return switch (area) {
            case JPN -> IS_JAPAN_BASE_NODE.failedRuleNames(ruleContext, fileContext);
            case USA -> IS_USA_BASE_NODE.failedRuleNames(ruleContext, fileContext);
            case EUR -> IS_EUROPE_BASE_NODE.failedRuleNames(ruleContext, fileContext);
        };
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

    private static RuleNode rule(String name, Rule rule) {
        return new RuleNode(name, rule, RuleOperator.LEAF, null, null);
    }

    private enum RuleOperator {
        LEAF,
        AND,
        OR
    }

    private record RuleNode(String name, Rule rule, RuleOperator operator, RuleNode left, RuleNode right) {
        private Rule asRule() {
            return rule;
        }

        private boolean pass(RuleContext ruleContext, FileContext fileContext) {
            return rule.pass(ruleContext, fileContext);
        }

        private RuleNode and(RuleNode other) {
            return new RuleNode("", rule.and(other.rule), RuleOperator.AND, this, other);
        }

        private RuleNode or(RuleNode other) {
            return new RuleNode("", rule.or(other.rule), RuleOperator.OR, this, other);
        }

        private List<String> failedRuleNames(RuleContext ruleContext, FileContext fileContext) {
            return switch (operator) {
                case LEAF -> rule.pass(ruleContext, fileContext) ? List.of() : List.of(name);
                case AND -> andFailedRuleNames(ruleContext, fileContext);
                case OR -> orFailedRuleNames(ruleContext, fileContext);
            };
        }

        private List<String> andFailedRuleNames(RuleContext ruleContext, FileContext fileContext) {
            var leftFailures = left.failedRuleNames(ruleContext, fileContext);
            // Diagnostics evaluate both sides so reports can show every failed AND rule.
            var rightFailures = right.failedRuleNames(ruleContext, fileContext);

            return Stream.concat(leftFailures.stream(), rightFailures.stream()).toList();
        }

        private List<String> orFailedRuleNames(RuleContext ruleContext, FileContext fileContext) {
            var leftFailures = left.failedRuleNames(ruleContext, fileContext);
            if (leftFailures.isEmpty()) {
                return List.of();
            }

            var rightFailures = right.failedRuleNames(ruleContext, fileContext);
            if (rightFailures.isEmpty()) {
                return List.of();
            }

            return Stream.concat(leftFailures.stream(), rightFailures.stream()).toList();
        }
    }
}

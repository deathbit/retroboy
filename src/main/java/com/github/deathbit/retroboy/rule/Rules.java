package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Rules {
    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    private static final Rule IS_LICENSED = (rc, fc) -> rc.getLicensed().contains(fc.getFullName());
    private static final Rule IS_BAD = (rc, fc) -> fc.getFullName().contains("[b]");
    private static final Rule IS_HIT_GLOBAL_TAG_BLACKLIST =
            (rc, fc) -> fc.getTags().stream().anyMatch(tag -> rc.getGlobalTagBlackList().contains(tag));
    private static final Rule IS_HIT_PLATFORM_TAG_BLACKLIST =
            (rc, fc) -> fc.getTags().stream().anyMatch(tag -> rc.getRuleConfig().getTagBlackList().contains(tag));
    private static final Rule IS_HIT_PLATFORM_FILE_NAME_BLACKLIST =
            (rc, fc) -> rc.getRuleConfig().getFileNameBlackList().contains(fc.getFileName());
    private static final Rule IS_PREVIOUS_REVISION =
            (rc, fc) -> newerRevisionFileName(rc, fc.getFileName()).isPresent();
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
            .and(IS_BAD.not())
            .and(IS_HIT_GLOBAL_TAG_BLACKLIST.not())
            .and(IS_HIT_PLATFORM_TAG_BLACKLIST.not())
            .and(IS_HIT_PLATFORM_FILE_NAME_BLACKLIST.not())
            .and(IS_PREVIOUS_REVISION.not());
    private static final Rule IS_HIT_AREA_FILE_NAME_BLACKLIST = (rc, fc) -> areaConfig(rc)
            .map(AreaConfig::getFileNameBlackList)
            .map(fileNameBlackList -> fileNameBlackList.contains(fc.getFileName()))
            .orElse(false);
    public static final Rule IS_JAPAN_BASE = IS_BASE.and(IS_JAPAN_OR_WORLD).and(IS_HIT_AREA_FILE_NAME_BLACKLIST.not());
    public static final Rule IS_USA_BASE = IS_BASE.and(IS_USA_OR_WORLD).and(IS_HIT_AREA_FILE_NAME_BLACKLIST.not());
    private static final Rule IS_EUROPE_BASE_WITHOUT_PREFERENCE =
            IS_BASE.and(IS_EUROPE_OR_WORLD).and(IS_HIT_AREA_FILE_NAME_BLACKLIST.not());
    private static final Rule PREFER_EUROPE_VERSION = (rc, fc) -> !IS_EUROPE_BASE_WITHOUT_PREFERENCE.pass(rc, fc)
            || isEuropeVersion(fc)
            || preferredEuropeVersionFileNames(rc, fc).isEmpty();
    public static final Rule IS_EUROPE_BASE = IS_EUROPE_BASE_WITHOUT_PREFERENCE.and(PREFER_EUROPE_VERSION);

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

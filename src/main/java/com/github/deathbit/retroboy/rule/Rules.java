package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.rule.complex.RuleIsHighestRevision;

import java.util.List;
import java.util.Objects;

public class Rules {
    private static final List<String> EUROPE_PAL_FALLBACK_REGIONS = List.of("France", "Australia", "Germany", "Spain", "Sweden");

    public static final Rule IS_WHITELIST = (rc, fc) -> {
        if (rc.getGlobalConfig().getGlobalRomWhitelist().contains(fc.getFileName())) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_WHITELIST失败: 不在全局ROM白名单中");
        return false;
    };
    public static final Rule IS_LICENSED = (rc, fc) -> {
        if (rc.getLicensed().contains(fc.getFullName())) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_LICENSED失败: DAT授权清单中不存在");
        return false;
    };
    public static final Rule IS_NOT_BAD = (rc, fc) -> {
        if (!fc.getFullName().contains("[b]")) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_NOT_BAD失败: 文件名包含坏档标记");
        return false;
    };
    public static final Rule IS_NOT_HITTING_GLOBAL_TAG_BLACKLIST = (rc, fc) -> {
        if (fc.getTags().stream().noneMatch(tag -> rc.getGlobalConfig().getGlobalTagBlacklist().contains(tag))) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_NOT_HITTING_GLOBAL_TAG_BLACKLIST失败: 命中全局标签黑名单");
        return false;
    };
    public static final Rule IS_NOT_HITTING_PLATFORM_TAG_BLACKLIST = (rc, fc) -> {
        if (fc.getTags().stream().noneMatch(tag -> rc.getPlatformPackTaskConfig().getTagBlackList().contains(tag))) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_NOT_HITTING_PLATFORM_TAG_BLACKLIST失败: 命中平台标签黑名单");
        return false;
    };
    public static final Rule IS_NOT_HITTING_PLATFORM_FILE_NAME_BLACKLIST = (rc, fc) -> {
        if (!rc.getPlatformPackTaskConfig().getFileNameBlackList().contains(fc.getFileName())) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_NOT_HITTING_PLATFORM_FILE_NAME_BLACKLIST失败: 命中平台文件名黑名单");
        return false;
    };
    public static final Rule IS_NOT_HITTING_PLATFORM_AREA_FILE_NAME_BLACKLIST = (rc, fc) -> {
        if (!rc.getCurrentAreaConfig().getAreaFileNameBlackList().contains(fc.getFileName())) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_NOT_HITTING_PLATFORM_AREA_FILE_NAME_BLACKLIST失败: 命中平台区域文件名黑名单");
        return false;
    };
    public static final Rule IS_HIGHEST_REVISION = new RuleIsHighestRevision();
    public static final Rule IS_JAPAN = (rc, fc) -> {
        if (fc.getTagPart().contains("Japan")) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_JAPAN失败: 不属于 Japan 地区");
        return false;
    };
    public static final Rule IS_USA = (rc, fc) -> {
        if (fc.getTagPart().contains("USA")) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_USA失败: 不属于 USA 地区");
        return false;
    };
    public static final Rule IS_EUROPE = (rc, fc) -> {
        if (fc.getTagPart().contains("Europe")) {
            return true;
        }
        if (rc.getPlatformPackTaskConfig().isUsePal() && findPalFallbackRegion(fc) != null) {
            return isPreferredPalFallbackRegion(rc, fc);
        }
        rc.getRomNotPassReasons().add("IS_EUROPE失败: 不属于 Europe 地区");
        return false;
    };
    public static final Rule IS_WORLD = (rc, fc) -> {
        if (fc.getTagPart().contains("World")) {
            return true;
        }
        rc.getRomNotPassReasons().add("IS_WORLD失败: 不属于 World 版本");
        return false;
    };
    public static final Rule IS_JAPAN_OR_WORLD = IS_JAPAN.or(IS_WORLD);
    public static final Rule IS_USA_OR_WORLD = IS_USA.or(IS_WORLD);
    public static final Rule IS_EUROPE_OR_WORLD = IS_EUROPE.or(IS_WORLD);
    public static final Rule IS_BASE = IS_LICENSED
        .and(IS_NOT_BAD)
        .and(IS_NOT_HITTING_GLOBAL_TAG_BLACKLIST)
        .and(IS_NOT_HITTING_PLATFORM_TAG_BLACKLIST)
        .and(IS_NOT_HITTING_PLATFORM_FILE_NAME_BLACKLIST)
        .and(IS_NOT_HITTING_PLATFORM_AREA_FILE_NAME_BLACKLIST)
        .and(IS_HIGHEST_REVISION);
    public static final Rule IS_BASE_WITH_ROM_WHITELIST = IS_WHITELIST.or(IS_BASE);
    public static final Rule IS_JAPAN_BASE = IS_BASE_WITH_ROM_WHITELIST.and(IS_JAPAN_OR_WORLD);
    public static final Rule IS_USA_BASE = IS_BASE_WITH_ROM_WHITELIST.and(IS_USA_OR_WORLD);
    public static final Rule IS_EUROPE_BASE = IS_BASE_WITH_ROM_WHITELIST.and(IS_EUROPE_OR_WORLD);

    private static boolean isPreferredPalFallbackRegion(RuleContext rc, FileContext fc) {
        var currentRegion = findPalFallbackRegion(fc);
        if (hasSameNameEuropeRom(rc, fc)) {
            rc.getRomNotPassReasons().add("IS_EUROPE失败: 存在同名 Europe ROM，忽略 PAL 备用地区 " + currentRegion);
            return false;
        }
        var preferredRegion = findPreferredPalFallbackRegion(rc, fc);
        if (currentRegion.equals(preferredRegion)) {
            return true;
        }
        rc.getRomNotPassReasons().add(String.format("IS_EUROPE失败: PAL 备用地区优先级低于 %s", preferredRegion));
        return false;
    }

    private static boolean hasSameNameEuropeRom(RuleContext rc, FileContext fc) {
        return rc.getFileContextMap().values().stream()
                .anyMatch(candidate -> fc.getNamePart().equals(candidate.getNamePart())
                        && candidate.getTagPart().contains("Europe"));
    }

    private static String findPreferredPalFallbackRegion(RuleContext rc, FileContext fc) {
        return rc.getFileContextMap().values().stream()
                .filter(candidate -> fc.getNamePart().equals(candidate.getNamePart()))
                .map(Rules::findPalFallbackRegion)
                .filter(Objects::nonNull)
                .min((left, right) -> Integer.compare(EUROPE_PAL_FALLBACK_REGIONS.indexOf(left), EUROPE_PAL_FALLBACK_REGIONS.indexOf(right)))
                .orElse(null);
    }

    private static String findPalFallbackRegion(FileContext fc) {
        return EUROPE_PAL_FALLBACK_REGIONS.stream()
                .filter(region -> fc.getTagPart().contains(region))
                .findFirst()
                .orElse(null);
    }
}

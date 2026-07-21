package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.rule.complex.RuleIsHighestRevision;

public class Rules {
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
}

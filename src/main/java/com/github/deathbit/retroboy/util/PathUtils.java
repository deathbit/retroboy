package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.enums.MediaAssetType;

import java.nio.file.Path;

public final class PathUtils {

    private PathUtils() {
    }

    // 默认 res 相对于运行工作目录；IDEA 中应将 Working directory 设置为项目根目录。
    public static final PathSupplier RESOURCES_HOME =
            ruleContext -> Path.of(ruleContext.getGlobalConfig().getResHome());

    // 示例：D:\ES-DE
    public static final PathSupplier ESDE_HOME =
            ruleContext -> Path.of(ruleContext.getGlobalConfig().getEsdeHome());

    // 示例：D:\ES-DE\Emulators\RetroArch-Win64
    public static final PathSupplier RETROARCH_HOME =
            ruleContext -> Path.of(ruleContext.getGlobalConfig().getRaHome());

    // 示例：res/platform/nes
    public static final PathSupplier PLATFORM_RESOURCE_ROOT =
            ruleContext -> RESOURCES_HOME.get(ruleContext)
                    .resolve("platform")
                    .resolve(ruleContext.getPlatform().getName());

    // 示例：res/platform/nes/roms
    public static final PathSupplier PLATFORM_ROMS =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("roms");

    // 示例：res/platform/nes/core_config
    public static final PathSupplier PLATFORM_CORE_CONFIG =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("core_config");

    // 示例：res/platform/nes/gamelist.xml
    public static final PathSupplier PLATFORM_GAMELIST_XML =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("gamelist.xml");

    // 示例：res/platform/nes/调试信息-NES.txt
    public static final PathSupplier DEBUG_REPORT =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("调试信息-" + ruleContext.getPlatform().name() + ".txt");

    // 示例：res/platform/nes/使用说明-NES.txt
    public static final PathSupplier RELEASE_REPORT =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("使用说明-" + ruleContext.getPlatform().name() + ".txt");

    // 示例：D:\ES-DE\ROMs
    public static final PathSupplier ESDE_ROMS_ROOT =
            ruleContext -> ESDE_HOME.get(ruleContext)
                    .resolve("ROMs");

    // 示例：D:\ES-DE\ROMs\nes
    public static final PathSupplier ESDE_PLATFORM_ROMS =
            ruleContext -> ESDE_ROMS_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatform().getName());

    // 示例：D:\ES-DE\ES-DE
    public static final PathSupplier ESDE_ROOT =
            ruleContext -> ESDE_HOME.get(ruleContext)
                    .resolve("ES-DE");

    // 示例：D:\ES-DE\ES-DE\downloaded_media
    public static final PathSupplier ESDE_MEDIA_ROOT =
            ruleContext -> ESDE_ROOT.get(ruleContext)
                    .resolve("downloaded_media");

    // 示例：D:\ES-DE\ES-DE\downloaded_media\nes
    public static final PathSupplier ESDE_PLATFORM_MEDIA =
            ruleContext -> ESDE_MEDIA_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatform().getName());

    // 示例：D:\ES-DE\ES-DE\gamelists
    public static final PathSupplier ESDE_GAMELISTS_ROOT =
            ruleContext -> ESDE_ROOT.get(ruleContext)
                    .resolve("gamelists");

    // 示例：D:\ES-DE\ES-DE\gamelists\nes
    public static final PathSupplier ESDE_PLATFORM_GAMELIST =
            ruleContext -> ESDE_GAMELISTS_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatform().getName());

    // 示例：D:\ES-DE\ES-DE\gamelists\nes\gamelist.xml
    public static final PathSupplier ESDE_PLATFORM_GAMELIST_XML =
            ruleContext -> ESDE_PLATFORM_GAMELIST.get(ruleContext)
                    .resolve("gamelist.xml");

    // 示例：D:\ES-DE\Emulators\RetroArch-Win64\config
    public static final PathSupplier RETROARCH_CONFIG =
            ruleContext -> RETROARCH_HOME.get(ruleContext)
                    .resolve("config");

    // 示例：res/release/NES.zip
    public static final PathSupplier RELEASE_ZIP =
            ruleContext -> RESOURCES_HOME.get(ruleContext)
                    .resolve("release")
                    .resolve(ruleContext.getPlatform().name() + ".zip");

    public static String string(PathSupplier pathSupplier, PlatformContext platformContext) {
        return pathSupplier.get(platformContext).toString();
    }

    public static Path platformCoreConfig(PlatformContext platformContext) {
        return PLATFORM_CORE_CONFIG.get(platformContext)
                .resolve(platformContext.getPlatformPackTaskConfig().getCore());
    }

    public static Path platformRom(PlatformContext platformContext, String rom) {
        return PLATFORM_ROMS.get(platformContext)
                .resolve(rom);
    }

    public static String esdeAreaDirectoryName(PlatformContext platformContext, String area) {
        var platformName = platformContext.getPlatform().name();
        var platformAlt = platformContext.getPlatformPackTaskConfig().getPlatformAlt();
        if (platformAlt == null || platformAlt.isBlank()) {
            return platformName + "-" + area;
        }
        return platformName + "-" + platformAlt + " - " + area;
    }

    public static Path esdeAreaRomDirectory(PlatformContext platformContext, String area) {
        return ESDE_PLATFORM_ROMS.get(platformContext)
                .resolve(esdeAreaDirectoryName(platformContext, area));
    }

    public static Path esdeAreaRom(PlatformContext platformContext, String area, String rom) {
        return esdeAreaRomDirectory(platformContext, area)
                .resolve(rom);
    }

    public static Path esdeMedia(PlatformContext platformContext,
                                 MediaAssetType mediaAssetType,
                                 String mediaAreaDirectoryName,
                                 String finalName,
                                 String extension) {
        return ESDE_PLATFORM_MEDIA.get(platformContext)
                .resolve(mediaAssetType.getDirectoryName())
                .resolve(mediaAreaDirectoryName)
                .resolve(finalName + "." + extension);
    }
}

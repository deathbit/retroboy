package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.MediaAssetType;

import java.nio.file.Path;

public final class PathUtils {

    private PathUtils() {
    }

    // 示例：D:\retroboy-resources
    public static final PathSupplier RESOURCES_HOME =
            ruleContext -> Path.of(ruleContext.getGlobalConfig().getResHome());

    // 示例：D:\ES-DE
    public static final PathSupplier ESDE_HOME =
            ruleContext -> Path.of(ruleContext.getGlobalConfig().getEsdeHome());

    // 示例：D:\ES-DE\Emulators\RetroArch-Win64
    public static final PathSupplier RETROARCH_HOME =
            ruleContext -> Path.of(ruleContext.getGlobalConfig().getRaHome());

    // 示例：D:\retroboy-resources\platform\nes
    public static final PathSupplier PLATFORM_RESOURCE_ROOT =
            ruleContext -> RESOURCES_HOME.get(ruleContext)
                    .resolve("platform")
                    .resolve(ruleContext.getPlatformName());

    // 示例：D:\retroboy-resources\platform\nes\roms
    public static final PathSupplier PLATFORM_ROMS =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("roms");

    // 示例：D:\retroboy-resources\platform\nes\dat\nes.dat
    public static final PathSupplier PLATFORM_DAT =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("dat")
                    .resolve(ruleContext.getPlatformName() + ".dat");

    // 示例：D:\retroboy-resources\platform\nes\nes_db.xml
    public static final PathSupplier PLATFORM_GAME_DB =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatformName() + "_db.xml");

    // 示例：D:\retroboy-resources\platform\nes\core_config
    public static final PathSupplier PLATFORM_CORE_CONFIG =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("core_config");

    // 示例：D:\retroboy-resources\platform\nes\downloaded_media\nes
    public static final PathSupplier PLATFORM_DOWNLOADED_MEDIA =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("downloaded_media")
                    .resolve(ruleContext.getPlatformName());

    // 示例：D:\retroboy-resources\platform\nes\gamelists\nes\gamelist.xml
    public static final PathSupplier PLATFORM_GAMELIST_XML =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("gamelists")
                    .resolve(ruleContext.getPlatformName())
                    .resolve("gamelist.xml");

    // 示例：D:\retroboy-resources\platform\nes\wiki
    public static final PathSupplier PLATFORM_WIKI_ROOT =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("wiki");

    // 示例：D:\retroboy-resources\platform\nes\wiki\NES-ROM.txt
    public static final PathSupplier PLATFORM_ROM_WIKI =
            ruleContext -> PLATFORM_WIKI_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatform().name() + "-ROM.txt");

    // 示例：D:\retroboy-resources\platform\nes\wiki\NES-WIKI-ROM.txt
    public static final PathSupplier PLATFORM_WIKI_ROM_MAPPING =
            ruleContext -> PLATFORM_WIKI_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatform().name() + "-WIKI-ROM.txt");

    // 示例：D:\retroboy-resources\platform\nes\report
    public static final PathSupplier PLATFORM_REPORT_ROOT =
            ruleContext -> PLATFORM_RESOURCE_ROOT.get(ruleContext)
                    .resolve("report");

    // 示例：D:\retroboy-resources\platform\nes\report\调试信息-NES.txt
    public static final PathSupplier DEBUG_REPORT =
            ruleContext -> PLATFORM_REPORT_ROOT.get(ruleContext)
                    .resolve("调试信息-" + ruleContext.getPlatform().name() + ".txt");

    // 示例：D:\retroboy-resources\platform\nes\report\使用说明-NES.txt
    public static final PathSupplier RELEASE_REPORT =
            ruleContext -> PLATFORM_REPORT_ROOT.get(ruleContext)
                    .resolve("使用说明-" + ruleContext.getPlatform().name() + ".txt");

    // 示例：D:\ES-DE\ROMs
    public static final PathSupplier ESDE_ROMS_ROOT =
            ruleContext -> ESDE_HOME.get(ruleContext)
                    .resolve("ROMs");

    // 示例：D:\ES-DE\ROMs\nes
    public static final PathSupplier ESDE_PLATFORM_ROMS =
            ruleContext -> ESDE_ROMS_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatformName());

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
                    .resolve(ruleContext.getPlatformName());

    // 示例：D:\ES-DE\ES-DE\gamelists
    public static final PathSupplier ESDE_GAMELISTS_ROOT =
            ruleContext -> ESDE_ROOT.get(ruleContext)
                    .resolve("gamelists");

    // 示例：D:\ES-DE\ES-DE\gamelists\nes
    public static final PathSupplier ESDE_PLATFORM_GAMELIST =
            ruleContext -> ESDE_GAMELISTS_ROOT.get(ruleContext)
                    .resolve(ruleContext.getPlatformName());

    // 示例：D:\ES-DE\ES-DE\gamelists\nes\gamelist.xml
    public static final PathSupplier ESDE_PLATFORM_GAMELIST_XML =
            ruleContext -> ESDE_PLATFORM_GAMELIST.get(ruleContext)
                    .resolve("gamelist.xml");

    // 示例：D:\ES-DE\Emulators\RetroArch-Win64\config
    public static final PathSupplier RETROARCH_CONFIG =
            ruleContext -> RETROARCH_HOME.get(ruleContext)
                    .resolve("config");

    // 示例：D:\retroboy-resources\release\NES.zip
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

    public static Path esdeAreaRomDirectory(PlatformContext platformContext, Area area) {
        return ESDE_PLATFORM_ROMS.get(platformContext)
                .resolve(platformContext.getPlatform().name() + "-" + area.name());
    }

    public static Path esdeAreaRom(PlatformContext platformContext, Area area, String rom) {
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

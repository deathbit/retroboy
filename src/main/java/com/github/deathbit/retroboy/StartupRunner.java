package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.handler.handlers.nintendo.NesHandler;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.deathbit.retroboy.utils.CommonUtils.printAsciiArt;
import static com.github.deathbit.retroboy.utils.CommonUtils.printTask;
import static com.github.deathbit.retroboy.utils.CommonUtils.printTaskDone;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ConfigComponent configComponent;

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private NesHandler nesHandler;

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        printAsciiArt();
        printTask("清理目录和文件", List.of(
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\info",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\autoconfig",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cheats",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\database",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\overlays",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders",
                "清空目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cores",
                "清空目录：D\\ES-DE\\Emulators\\RetroArch-Win64\\system",
                "清空目录：D:\\ES-DE\\ROMs",
                "删除文件：D:\\ES-DE\\Emulators\\RetroArch-Win64\\retroarch.cfg"
        ));
        fileComponent.batchCleanDirs(appConfig.getCleanUpTask().getCleanDirs());
        fileComponent.batchDeleteFiles(appConfig.getCleanUpTask().getDeleteFiles());
        printTaskDone("清理目录和文件");

        printTask("默认配置", List.of(
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\info\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\info",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\assets\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\autoconfig\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\autoconfig",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\cheats\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\cheats",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\database\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\database",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\overlays\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\overlays",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\shaders\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\cores\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\cores",
                "拷贝目录内容：D:\\Resources\\RetroArch-Win64\\system\\* -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\system",
                "拷贝文件：D:\\Resources\\retroarch.cfg -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\retroarch.cfg",
                "设置RA选项：video_fullscreen = \"true\"",
                "设置RA选项：rgui_browser_directory = \"D:\\ES-DE\\ROMs\"",
                "设置RA选项：input_player1_analog_dpad_mode = \"1\""
        ));
        fileComponent.batchCopyDirContentsToDirs(appConfig.getDefaultConfigTask().getCopyDirContentsInputs());
        fileComponent.batchCopyFiles(appConfig.getDefaultConfigTask().getCopyFileInputs());
        configComponent.batchChangeRaConfigs(appConfig.getDefaultConfigTask().getRaConfigInputs());
        printTaskDone("默认配置");

        printTask("修复中文字体", List.of(
                "删除文件：D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets\\pkg\\chinese-fallback-font.ttf",
                "拷贝文件：D:\\Resources\\chinese-fallback-font.ttf -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets\\pkg\\chinese-fallback-font.ttf",
                "设置RA选项：video_font_path = \":\\assets\\pkg\\chinese-fallback-font.ttf\""
        ));
        fileComponent.batchDeleteFiles(List.of(appConfig.getFixChineseFontTask().getDeleteOriginalFontFile()));
        fileComponent.batchCopyFiles(List.of(appConfig.getFixChineseFontTask().getCopyNewFontFile()));
        configComponent.batchChangeRaConfigs(List.of(appConfig.getFixChineseFontTask().getSetNotificationFont()));
        printTaskDone("修复中文字体");

        printTask("设置Mega Bezel着色器", List.of(
                "拷贝目录；D:\\Resources\\Mega_Bezel_Packs -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders\\Mega_Bezel_Packs",
                "拷贝文件：D:\\Resources\\global.slangp -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\config\\global.slangp",
                "拷贝文件：D:\\Resources\\retroarch.slangp -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders\\retroarch.slangp",
                "设置RA选项：video_driver = \"vulkan\"",
                "设置RA选项：aspect_ratio_index = \"24\"",
                "设置RA选项：video_scale_integer = \"false\"",
                "设置RA选项：video_rotation = \"0\"",
                "设置RA选项：video_allow_rotate = \"false\"",
                "设置RA选项：video_shader_enable = \"true\""
        ));
        fileComponent.batchCopyDirs(List.of(appConfig.getSetMegaBezelShaderTask().getCopyMegaBezelPacks()));
        fileComponent.batchCopyFiles(appConfig.getSetMegaBezelShaderTask().getCopyDefaultMegaBezelShader());
        configComponent.batchChangeRaConfigs(appConfig.getSetMegaBezelShaderTask().getSetMegaBezelShaderConfigInputs());
        printTaskDone("设置Mega Bezel着色器");

        printTask("设置平台", List.of(
                "设置NES"
        ));
        nesHandler.handle();
        printTaskDone("设置平台");
    }
}

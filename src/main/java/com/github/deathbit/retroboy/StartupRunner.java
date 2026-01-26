package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.Config;
import com.github.deathbit.retroboy.handler.handlers.NesHandler;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.deathbit.retroboy.Utils.printTask;
import static com.github.deathbit.retroboy.Utils.printTaskDone;

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
        printTask("删除目录和文件", List.of(
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\info",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\autoconfig",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cheats",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\database",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\overlays",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cores",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\system",
                "清理目录：D:\\ES-DE\\ROMs",
                "删除文件：D:\\ES-DE\\Emulators\\RetroArch-Win64\\retroarch.cfg"
        ));
        fileComponent.batchDeleteDirContent(appConfig.getCleanUpTask().getDeleteContentDirs());
        fileComponent.batchDeleteFile(appConfig.getCleanUpTask().getDeleteFiles());
        printTaskDone("删除目录和文件");

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
        fileComponent.batchCopyDirContent(appConfig.getDefaultConfigTask().getCopyContentDirs());
        fileComponent.batchCopyFile(appConfig.getDefaultConfigTask().getCopyFiles());
        configComponent.batchChangeConfig(appConfig.getDefaultConfigTask().getRaConfigs());
        printTaskDone("默认配置");

        printTask("修复中文字体", List.of(
                "删除文件：D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets\\pkg\\chinese-fallback-font.ttf",
                "拷贝文件：D:\\Resources\\chinese-fallback-font.ttf -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets\\pkg\\chinese-fallback-font.ttf",
                "设置RA选项：video_font_path = \":\\assets\\pkg\\chinese-fallback-font.ttf\""
        ));
        fileComponent.deleteFile(appConfig.getFixChineseFontTask().getDeleteFontFile());
        fileComponent.copyFile(appConfig.getFixChineseFontTask().getCopyFontFile());
        configComponent.changeConfig(appConfig.getFixChineseFontTask().getSetNotificationFont());
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
        fileComponent.copyDir(CopyDir.builder().src("D:\\Resources\\Mega_Bezel_Packs").dest("D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders").build());
        fileComponent.copyFile(CopyFile.builder().srcFile("D:\\Resources\\global.slangp").destDir("D:\\ES-DE\\Emulators\\RetroArch-Win64\\config").build());
        fileComponent.copyFile(CopyFile.builder().srcFile("D:\\Resources\\retroarch.slangp").destDir("D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders").build());
        configComponent.batchChangeConfig(List.of(
                Config.builder().configFile(appConfig.getRetroArchConfig()).key("video_driver").value("vulkan").build(),
                Config.builder().configFile(appConfig.getRetroArchConfig()).key("aspect_ratio_index").value("24").build(),
                Config.builder().configFile(appConfig.getRetroArchConfig()).key("video_scale_integer").value("false").build(),
                Config.builder().configFile(appConfig.getRetroArchConfig()).key("video_rotation").value("0").build(),
                Config.builder().configFile(appConfig.getRetroArchConfig()).key("video_allow_rotate").value("false").build(),
                Config.builder().configFile(appConfig.getRetroArchConfig()).key("video_shader_enable").value("true").build()
        ));
        printTaskDone("设置Mega Bezel着色器");

        nesHandler.handle();
    }
}

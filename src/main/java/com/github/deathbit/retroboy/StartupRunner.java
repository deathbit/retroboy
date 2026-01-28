package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.GlobalConfig;
import com.github.deathbit.retroboy.domain.*;
import com.github.deathbit.retroboy.handler.handlers.nintendo.NesHandler;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
        GlobalConfig globalConfig = appConfig.getGlobalConfig();

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

        // Resolve paths for cleanup task
        List<Path> resolvedDeleteFiles = appConfig.getCleanUpTask().getDeleteFiles().stream()
                .map(file -> Paths.get(globalConfig.getRaHome(), file))
                .collect(Collectors.toList());

        List<Path> resolvedDeleteContentDirs = appConfig.getCleanUpTask().getDeleteContentDirs().stream()
                .map(dir -> {
                    // ROMs is under esdeHome, others under raHome
                    if ("ROMs".equals(dir)) {
                        return Paths.get(globalConfig.getEsdeHome(), dir);
                    } else {
                        return Paths.get(globalConfig.getRaHome(), dir);
                    }
                })
                .collect(Collectors.toList());

        fileComponent.batchCleanDirs(resolvedDeleteContentDirs);
        fileComponent.batchDeleteFiles(resolvedDeleteFiles);
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

        // Resolve paths for copy content dirs
        List<CopyDirContentsInput> resolvedCopyContentDirs = appConfig.getDefaultConfigTask().getCopyContentDirs().stream()
                .map(input -> {
                    Path srcDir;
                    Path destDir;

                    // ROMs_ALL is under esdeHome
                    if ("ROMs_ALL".equals(input.getSrcDir().toString())) {
                        srcDir = Paths.get(globalConfig.getEsdeHome()).resolve(input.getSrcDir());
                    } else {
                        srcDir = Paths.get(globalConfig.getResourcesHome()).resolve(input.getSrcDir());
                    }

                    // ROMs dest is under esdeHome, others under raHome
                    if ("ROMs".equals(input.getDestDir().toString())) {
                        destDir = Paths.get(globalConfig.getEsdeHome()).resolve(input.getDestDir());
                    } else {
                        destDir = Paths.get(globalConfig.getRaHome()).resolve(input.getDestDir());
                    }

                    return CopyDirContentsInput.builder()
                            .srcDir(srcDir)
                            .destDir(destDir)
                            .build();
                })
                .collect(Collectors.toList());

        // Resolve paths for copy files
        List<CopyFileInput> resolvedCopyFiles = appConfig.getDefaultConfigTask().getCopyFiles().stream()
                .map(input -> {
                    Path srcFile = Paths.get(globalConfig.getResourcesHome()).resolve(input.getSrcFile());
                    Path destDir = input.getDestDir().toString().isEmpty()
                            ? Paths.get(globalConfig.getRaHome())
                            : Paths.get(globalConfig.getRaHome()).resolve(input.getDestDir());
                    return CopyFileInput.builder()
                            .srcFile(srcFile)
                            .destDir(destDir)
                            .build();
                })
                .collect(Collectors.toList());

        // Resolve paths for RA configs
        List<ConfigInput> resolvedRaConfigInputs = appConfig.getDefaultConfigTask().getRaConfigInputs().stream()
                .map(config -> {
                    String file = Paths.get(globalConfig.getRaHome(), config.getFile()).toString();
                    String value = config.getValue();

                    // Special handling for rgui_browser_directory - it needs full esdeHome path
                    if ("rgui_browser_directory".equals(config.getKey())) {
                        value = Paths.get(globalConfig.getEsdeHome(), config.getValue()).toString();
                    }

                    return ConfigInput.builder()
                            .file(file)
                            .key(config.getKey())
                            .value(value)
                            .build();
                })
                .collect(Collectors.toList());

        fileComponent.batchCopyDirContentsToDirs(resolvedCopyContentDirs);
        fileComponent.batchCopyFiles(resolvedCopyFiles);
        configComponent.batchChangeRaConfigs(resolvedRaConfigInputs);
        printTaskDone("默认配置");

        printTask("修复中文字体", List.of(
                "删除文件：D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets\\pkg\\chinese-fallback-font.ttf",
                "拷贝文件：D:\\Resources\\chinese-fallback-font.ttf -> D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets\\pkg\\chinese-fallback-font.ttf",
                "设置RA选项：video_font_path = \":\\assets\\pkg\\chinese-fallback-font.ttf\""
        ));

        // Resolve paths for fix chinese font task
        Path deleteFontFile = Paths.get(globalConfig.getRaHome(),
                appConfig.getFixChineseFontTask().getDeleteFontFile());

        CopyFileInput copyFontFile = CopyFileInput.builder()
                .srcFile(Paths.get(globalConfig.getResourcesHome()).resolve(
                        appConfig.getFixChineseFontTask().getCopyFontFile().getSrcFile()))
                .destDir(Paths.get(globalConfig.getRaHome()).resolve(
                        appConfig.getFixChineseFontTask().getCopyFontFile().getDestDir()))
                .build();

        ConfigInput setNotificationFont = ConfigInput.builder()
                .file(Paths.get(globalConfig.getRaHome(),
                        appConfig.getFixChineseFontTask().getSetNotificationFont().getFile()).toString())
                .key(appConfig.getFixChineseFontTask().getSetNotificationFont().getKey())
                .value(appConfig.getFixChineseFontTask().getSetNotificationFont().getValue())
                .build();

        fileComponent.batchDeleteFiles(List.of(deleteFontFile));
        fileComponent.batchCopyFiles(List.of(copyFontFile));
        configComponent.changeConfig(setNotificationFont);
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

        // Resolve paths for Mega Bezel shader task
        var copyMegaBezelPacks = appConfig.getSetMegaBezelShaderTask().getCopyMegaBezelPacks();
        var resolvedCopyMegaBezelPacks = CopyDirInput.builder()
                .srcDir(Paths.get(globalConfig.getResourcesHome()).resolve(copyMegaBezelPacks.getSrcDir()))
                .destDir(Paths.get(globalConfig.getRaHome()).resolve(copyMegaBezelPacks.getDestDir()))
                .build();

        List<CopyFileInput> resolvedCopyDefaultMegaBezelShader = appConfig.getSetMegaBezelShaderTask()
                .getCopyDefaultMegaBezelShader().stream()
                .map(input -> CopyFileInput.builder()
                        .srcFile(Paths.get(globalConfig.getResourcesHome()).resolve(input.getSrcFile()))
                        .destDir(Paths.get(globalConfig.getRaHome()).resolve(input.getDestDir()))
                        .build())
                .collect(Collectors.toList());

        List<ConfigInput> resolvedSetMegaBezelShaderConfigInputs = appConfig.getSetMegaBezelShaderTask()
                .getSetMegaBezelShaderConfigInputs().stream()
                .map(config -> ConfigInput.builder()
                        .file(Paths.get(globalConfig.getRaHome(), config.getFile()).toString())
                        .key(config.getKey())
                        .value(config.getValue())
                        .build())
                .collect(Collectors.toList());

        fileComponent.batchCopyDirs(List.of(resolvedCopyMegaBezelPacks));
        fileComponent.batchCopyFiles(resolvedCopyDefaultMegaBezelShader);
        configComponent.batchChangeRaConfigs(resolvedSetMegaBezelShaderConfigInputs);
        printTaskDone("设置Mega Bezel着色器");

        printTask("设置平台", List.of());
        nesHandler.handle(HandlerInput.builder().appConfig(appConfig).fileComponent(fileComponent).build());
        printTaskDone("设置平台");
    }
}

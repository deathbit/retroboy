package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import com.github.deathbit.retroboy.component.CopyComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupRunner implements ApplicationRunner {

    private final AppConfig appConfig;
    private final CleanUpComponent cleanUpComponent;
    private final CopyComponent copyComponent;

    public StartupRunner(
            AppConfig appConfig,
            CleanUpComponent cleanUpComponent,
            CopyComponent copyComponent) {
        this.appConfig = appConfig;
        this.cleanUpComponent = cleanUpComponent;
        this.copyComponent = copyComponent;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        Utils.printTaskArt("清理目录", List.of(
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\info",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\assets",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\autoconfig",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cheats",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\database",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\overlays",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\shaders",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\cores",
                "清理目录：D:\\ES-DE\\Emulators\\RetroArch-Win64\\system",
                "清理目录：D:\\ES-DE\\ROMs"
        ));
        cleanUpComponent.batchCleanupDir(appConfig.getCleanup().getCleanupDirs());

        Utils.printTaskArt("删除文件", List.of(
                "删除文件：D:\\ES-DE\\Emulators\\RetroArch-Win64\\retroarch.cfg"
        ));
        cleanUpComponent.batchDeleteFile(appConfig.getCleanup().getDeleteFiles());

        Utils.printTaskArt("默认配置", List.of(
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\info",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\assets",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\autoconfig",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\cheats",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\database",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\overlays",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\shaders",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\cores",
                "拷贝目录：D:\\Resources\\RetroArch-Win64\\system",
                "拷贝文件：D:\\Resources\\retroarch.cfg"
        ));
        copyComponent.batchCopyDirContent(appConfig.getCopyDefault().getCopyDirs());
        copyComponent.batchCopyFile(appConfig.getCopyDefault().getCopyFiles());
    }
}

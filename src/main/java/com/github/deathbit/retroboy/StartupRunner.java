package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.tasks.CleanUpTask;
import com.github.deathbit.retroboy.config.tasks.DefaultConfigTask;
import com.github.deathbit.retroboy.config.tasks.FixChineseFontTask;
import com.github.deathbit.retroboy.config.tasks.SetMegaBezelShaderTask;
import com.github.deathbit.retroboy.domain.*;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.Handler;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    private List<Handler> handlers;

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        buildingBasePack();
        buildingPlatformPack();
    }

    private void buildingBasePack() throws Exception {
        printAsciiArt();
//        runStartupTask(BasePackTask.CLEAN_UP, "清理目录和文件", this::describeCleanUpTask, () -> {
//            fileComponent.batchCleanDirs(appConfig.getCleanUpTask().getCleanDirs());
//            fileComponent.batchDeleteFiles(appConfig.getCleanUpTask().getDeleteFiles());
//        });
//        runStartupTask(BasePackTask.DEFAULT_CONFIG, "默认配置", this::describeDefaultConfigTask, () -> {
//            fileComponent.batchCopyDirContentsToDirs(appConfig.getDefaultConfigTask().getCopyDirContentsInputs());
//            fileComponent.batchCopyFiles(appConfig.getDefaultConfigTask().getCopyFileInputs());
//            configComponent.batchChangeRaConfigs(appConfig.getDefaultConfigTask().getRaConfigInputs());
//        });
//        runStartupTask(BasePackTask.FIX_CHINESE_FONT, "修复中文字体", this::describeFixChineseFontTask, () -> {
//            fileComponent.batchDeleteFiles(List.of(appConfig.getFixChineseFontTask().getDeleteOriginalFontFile()));
//            fileComponent.batchCopyFiles(List.of(appConfig.getFixChineseFontTask().getCopyNewFontFile()));
//            configComponent.batchChangeRaConfigs(List.of(appConfig.getFixChineseFontTask().getSetNotificationFont()));
//        });
//        runStartupTask(BasePackTask.SET_MEGA_BEZEL_SHADER, "设置Mega Bezel着色器", this::describeSetMegaBezelShaderTask, () -> {
//            fileComponent.batchCopyDirs(List.of(appConfig.getSetMegaBezelShaderTask().getCopyMegaBezelPacks()));
//            fileComponent.batchCopyFiles(appConfig.getSetMegaBezelShaderTask().getCopyDefaultMegaBezelShader());
//            configComponent.batchChangeRaConfigs(appConfig.getSetMegaBezelShaderTask().getSetMegaBezelShaderConfigInputs());
//        });
    }

    private void buildingPlatformPack() throws Exception {
        if (isTaskEnabled(BasePackTask.SET_PLATFORM)) {
            runSetPlatformTask();
        }
    }

    private void runStartupTask(BasePackTask basePackTask, String taskName, Supplier<List<String>> taskDescriptionSupplier, RunnableWithException runner) throws Exception {
        if (!isTaskEnabled(basePackTask)) {
            return;
        }

        printTask(taskName, taskDescriptionSupplier.get());
        runner.run();
        printTaskDone(taskName);
    }

    private boolean isTaskEnabled(BasePackTask basePackTask) {
        return BasePackTask.isEnabled(basePackTask, appConfig.getGlobalConfig().getStartupTaskMask());
    }

    private boolean isPlatformEnabled(Platform platform) {
        return Platform.isEnabled(platform, appConfig.getGlobalConfig().getPlatformTaskMask());
    }

    private List<String> describeCleanUpTask() {
        CleanUpTask task = appConfig.getCleanUpTask();
        List<String> descriptions = task.getCleanDirs().stream()
                .map(dir -> "清空目录：" + dir)
                .toList();
        return Stream.concat(
                descriptions.stream(),
                task.getDeleteFiles().stream().map(file -> "删除文件：" + file)
        ).toList();
    }

    private List<String> describeDefaultConfigTask() {
        DefaultConfigTask task = appConfig.getDefaultConfigTask();
        return Stream.of(
                task.getCopyDirContentsInputs().stream().map(this::describeCopyDirContents),
                task.getCopyFileInputs().stream().map(this::describeCopyFile),
                task.getRaConfigInputs().stream().map(this::describeRaConfig)
        ).flatMap(stream -> stream).toList();
    }

    private List<String> describeFixChineseFontTask() {
        FixChineseFontTask task = appConfig.getFixChineseFontTask();
        return List.of(
                "删除文件：" + task.getDeleteOriginalFontFile(),
                describeCopyFile(task.getCopyNewFontFile()),
                describeRaConfig(task.getSetNotificationFont())
        );
    }

    private List<String> describeSetMegaBezelShaderTask() {
        SetMegaBezelShaderTask task = appConfig.getSetMegaBezelShaderTask();
        return Stream.of(
                Stream.of(describeCopyDir(task.getCopyMegaBezelPacks())),
                task.getCopyDefaultMegaBezelShader().stream().map(this::describeCopyFile),
                task.getSetMegaBezelShaderConfigInputs().stream().map(this::describeRaConfig)
        ).flatMap(stream -> stream).toList();
    }

    private void runSetPlatformTask() throws Exception {
        for (var handler : getEnabledPlatformHandlers()) {
            var taskName = "设置" + handler.getPlatform().name();
            printTask(taskName, null);
            handler.handle();
            printTaskDone(taskName);
        }
    }

    private List<Handler> getEnabledPlatformHandlers() {
        return handlers.stream()
                .filter(handler -> isPlatformEnabled(handler.getPlatform()))
                .sorted(Comparator.comparingInt(handler -> handler.getPlatform().ordinal()))
                .toList();
    }

    private String describeCopyDir(CopyDirInput input) {
        return "拷贝目录：%s -> %s".formatted(input.getSrcDir(), input.getDestDir());
    }

    private String describeCopyDirContents(CopyDirContentsInput input) {
        return "拷贝目录内容：%s\\* -> %s".formatted(input.getSrcDir(), input.getDestDir());
    }

    private String describeCopyFile(CopyFileInput input) {
        return "拷贝文件：%s -> %s".formatted(input.getSrcFile(), input.getDestDir());
    }

    private String describeRaConfig(ConfigInput input) {
        return "设置RA选项：%s = \"%s\"".formatted(input.getKey(), input.getValue());
    }

}

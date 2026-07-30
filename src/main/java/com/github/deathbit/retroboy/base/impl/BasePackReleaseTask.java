package com.github.deathbit.retroboy.base.impl;

import com.github.deathbit.retroboy.base.BasePackHandler;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class BasePackReleaseTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ReleaseComponent releaseComponent;

    @Override
    public String name() {
        return appConfig.getBasePackReleaseTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getBasePackReleaseTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.BASE_PACK_RELEASE_TASK;
    }

    @Override
    public void handle() throws Exception {
        var targetPath = buildVersionedTargetPath();
        fileComponent.deletePath(targetPath);
        releaseComponent.release(
                targetPath,
                List.of(Path.of(appConfig.getGlobalConfig().getEsdeHome())),
                appConfig.getBasePackReleaseTaskConfig().getRootFilePaths()
        );
    }

    private Path buildVersionedTargetPath() {
        var targetPath = appConfig.getBasePackReleaseTaskConfig().getTargetPath();
        var fileName = targetPath.getFileName().toString();
        var extensionIndex = fileName.lastIndexOf('.');
        var version = appConfig.getBasePackReleaseReportTaskConfig().getBasePackVersion();
        var fileNameWithoutExtension = extensionIndex == -1 ? fileName : fileName.substring(0, extensionIndex);
        if (fileNameWithoutExtension.endsWith("_" + version)) {
            return targetPath;
        }
        var versionedFileName = extensionIndex == -1
                ? fileName + "_" + version
                : fileNameWithoutExtension + "_" + version + fileName.substring(extensionIndex);
        var parent = targetPath.getParent();
        return parent == null ? Path.of(versionedFileName) : parent.resolve(versionedFileName);
    }
}

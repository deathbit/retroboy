package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReleaseHandler {

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ReleaseComponent releaseComponent;
    public void handle(PlatformContext platformContext) throws Exception {
        if (platformContext.getPlatformPackTaskConfig().isRelease()) {
            var targetPath = buildVersionedTargetPath(platformContext);
            fileComponent.deletePath(targetPath);
            var sourcePaths = new ArrayList<>(platformContext.getPlatformPackTaskConfig().getCoreConfigs());
            sourcePaths.addAll(List.of(
                    PathUtils.ESDE_PLATFORM_ROMS.get(platformContext),
                    PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext),
                    PathUtils.ESDE_PLATFORM_GAMELIST.get(platformContext)));
            releaseComponent.release(targetPath, sourcePaths, buildRootFilePaths(platformContext));
        }
    }

    private Path buildVersionedTargetPath(PlatformContext platformContext) {
        var targetPath = PathUtils.RELEASE_ZIP.get(platformContext);
        var fileName = targetPath.getFileName().toString();
        var extensionIndex = fileName.lastIndexOf('.');
        var version = platformContext.getPlatformPackTaskConfig().getVersion();
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

    private List<Path> buildRootFilePaths(PlatformContext platformContext) {
        return List.of(
                PathUtils.RESOURCES_HOME.get(platformContext).resolve("微信赞赏码.png"),
                PathUtils.RESOURCES_HOME.get(platformContext).resolve("支付宝收款码.jpg"),
                PathUtils.DEBUG_REPORT.get(platformContext),
                PathUtils.RELEASE_REPORT.get(platformContext)
        );
    }
}

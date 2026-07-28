package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.domain.RuleContext;
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
    public void handle(RuleContext ruleContext) throws Exception {
        if (ruleContext.getPlatformPackTaskConfig().isRelease()) {
            var targetPath = buildVersionedTargetPath(ruleContext);
            fileComponent.deletePath(targetPath);
            var sourcePaths = new ArrayList<>(ruleContext.getPlatformPackTaskConfig().getCoreConfigs());
            sourcePaths.addAll(List.of(
                    PathUtils.ESDE_PLATFORM_ROMS.get(ruleContext),
                    PathUtils.ESDE_PLATFORM_MEDIA.get(ruleContext),
                    PathUtils.ESDE_PLATFORM_GAMELIST.get(ruleContext)));
            releaseComponent.release(targetPath, sourcePaths, buildRootFilePaths(ruleContext));
        }
    }

    private Path buildVersionedTargetPath(RuleContext ruleContext) {
        var targetPath = PathUtils.RELEASE_ZIP.get(ruleContext);
        var fileName = targetPath.getFileName().toString();
        var extensionIndex = fileName.lastIndexOf('.');
        var version = ruleContext.getPlatformPackTaskConfig().getVersion();
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

    private List<Path> buildRootFilePaths(RuleContext ruleContext) {
        return List.of(
                PathUtils.RESOURCES_HOME.get(ruleContext).resolve("微信赞赏码.png"),
                PathUtils.RESOURCES_HOME.get(ruleContext).resolve("支付宝收款码.jpg"),
                PathUtils.DEBUG_REPORT.get(ruleContext),
                PathUtils.RELEASE_REPORT.get(ruleContext)
        );
    }
}

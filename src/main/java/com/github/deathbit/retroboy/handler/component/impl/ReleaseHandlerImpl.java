package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.ReleaseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ReleaseHandlerImpl implements ReleaseHandler {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        var romDirName = ruleContext.getRuleConfig().getRomDirName();
        var defaultCore = ruleContext.getRuleConfig().getDefaultCore();
        var downloadedMediaDirBase = appConfig.getGlobalConfig().getDownloadedMediaDirBase();
        var romsDirBase = appConfig.getGlobalConfig().getRomsDir();

        var downloadedMediaDir = Paths.get(downloadedMediaDirBase, romDirName);
        var gamelistFile = Paths.get(downloadedMediaDirBase)
                                .getParent()
                                .resolve("gamelists")
                                .resolve(romDirName)
                                .resolve("gamelist.xml");
        var romsDir = Paths.get(romsDirBase, romDirName);
        var coreConfigDir = Paths.get(appConfig.getGlobalConfig().getRaConfig()).getParent().resolve("config").resolve(defaultCore);
        var coreOptionFile = coreConfigDir.resolve(defaultCore + ".opt");
        var coreShaderFile = coreConfigDir.resolve(defaultCore + ".slangp");
        var releaseDir = Paths.get("release");
        Files.createDirectories(releaseDir);
        var releaseFile = releaseDir.resolve(ruleContext.getPlatform().name() + ".zip");
        deletePreviousReleaseFile(releaseFile);

        var commonRoot = Paths.get(romsDirBase).getParent().getParent();
        zip(releaseFile, commonRoot, List.of(downloadedMediaDir, gamelistFile, romsDir, coreOptionFile, coreShaderFile));
    }

    private void deletePreviousReleaseFile(Path releaseFile) throws Exception {
        ProgressBar pb = new ProgressBar("删除旧包");
        pb.startTask(1);
        Files.deleteIfExists(releaseFile);
        pb.updateTask(0);
        pb.finishTaskAndClose();
    }

    private void zip(Path releaseFile, Path commonRoot, List<Path> sources) throws Exception {
        ProgressBar pb = new ProgressBar("生成新包", sources.size());
        try (var outputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(
                releaseFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)))) {
            for (var source : sources) {
                addSource(outputStream, commonRoot, source, pb);
            }
        } finally {
            pb.close();
        }
    }

    private void addSource(ZipOutputStream outputStream, Path commonRoot, Path source, ProgressBar pb) throws Exception {
        if (Files.isRegularFile(source)) {
            pb.startTask(1);
            addFile(outputStream, commonRoot, source);
            pb.updateTask(0);
            pb.finishTask();
            return;
        }

        try (var paths = Files.walk(source)) {
            var sourcePaths = paths.sorted(Comparator.naturalOrder()).toList();
            pb.startTask(sourcePaths.size());
            for (int i = 0; i < sourcePaths.size(); i++) {
                var path = sourcePaths.get(i);
                if (Files.isDirectory(path)) {
                    addDirectory(outputStream, commonRoot, path);
                } else if (Files.isRegularFile(path)) {
                    addFile(outputStream, commonRoot, path);
                }
                pb.updateTask(i);
            }
            pb.finishTask();
        }
    }

    private void addDirectory(ZipOutputStream outputStream, Path commonRoot, Path directory) throws Exception {
        var entryName = zipEntryName(commonRoot, directory) + "/";
        outputStream.putNextEntry(new ZipEntry(entryName));
        outputStream.closeEntry();
    }

    private void addFile(ZipOutputStream outputStream, Path commonRoot, Path file) throws Exception {
        outputStream.putNextEntry(new ZipEntry(zipEntryName(commonRoot, file)));
        Files.copy(file, outputStream);
        outputStream.closeEntry();
    }

    private String zipEntryName(Path commonRoot, Path path) {
        return commonRoot.relativize(path).toString().replace("\\", "/");
    }
}

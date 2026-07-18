package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ReleaseComponentImpl implements ReleaseComponent {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int PROGRESS_UPDATE_INTERVAL = 100;

    @Override
    public void release(String fileName, PathPair pathPair) throws Exception {
        var sourcePath = Paths.get(pathPair.getSourcePath());
        var targetPath = Paths.get(pathPair.getTargetPath());
        Files.createDirectories(targetPath);

        var releaseFile = targetPath.resolve(fileName);
        var commonRoot = sourcePath.getParent();
        zip(releaseFile, commonRoot, sourcePath);
    }

    private void zip(Path releaseFile, Path commonRoot, Path sourcePath) throws Exception {
        ProgressBar pb = new ProgressBar("发布新包");
        int totalFiles;
        try (var paths = Files.walk(sourcePath)) {
            totalFiles = Math.toIntExact(paths.filter(Files::isRegularFile).count());
        }

        pb.startTask(totalFiles);
        try (var outputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(
                releaseFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE))) {
            outputStream.setLevel(Deflater.BEST_SPEED);
            int finishedFiles = 0;
            try (var paths = Files.walk(sourcePath)) {
                var iterator = paths.iterator();
                while (iterator.hasNext()) {
                    var path = iterator.next();
                    if (!Files.isRegularFile(path)) {
                        continue;
                    }

                    addFile(outputStream, commonRoot, path);
                    finishedFiles++;
                    if (finishedFiles % PROGRESS_UPDATE_INTERVAL == 0 || finishedFiles == totalFiles) {
                        pb.updateTask(finishedFiles - 1);
                    }
                }
            }
            pb.finishTask();
        } finally {
            pb.close();
        }
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

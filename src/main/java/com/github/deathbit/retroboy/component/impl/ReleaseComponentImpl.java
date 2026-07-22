package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ReleaseComponentImpl implements ReleaseComponent {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int PROGRESS_UPDATE_INTERVAL = 100;

    @Override
    public void releaseNew(String targetPath, List<String> sourcePaths) {
        try {
            var releaseFile = Paths.get(targetPath);
            var releaseDir = releaseFile.getParent();
            if (releaseDir != null) {
                Files.createDirectories(releaseDir);
            }
            var sources = collectSourcePaths(sourcePaths, releaseFile);
            zip(releaseFile, sources);
        } catch (Exception e) {
            throw new RuntimeException("Failed to release new package", e);
        }
    }

    private void zip(Path releaseFile, List<Path> sourcePaths) throws Exception {
        ProgressBar pb = new ProgressBar("发布新包");
        pb.startTask(sourcePaths.size());
        try (var outputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(
                releaseFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE))) {
            outputStream.setLevel(Deflater.BEST_SPEED);
            for (int i = 0; i < sourcePaths.size(); i++) {
                var path = sourcePaths.get(i);
                if (Files.isDirectory(path)) {
                    addDirectory(outputStream, path);
                } else if (Files.isRegularFile(path)) {
                    addFile(outputStream, path);
                }
                pb.updateTask(i);
            }
            pb.finishTask();
        } finally {
            pb.close();
        }
    }

    private List<Path> collectSourcePaths(List<String> sourcePaths, Path releaseFile) throws Exception {
        var collectedPaths = new LinkedHashMap<Path, Path>();
        var releaseFilePath = releaseFile.toAbsolutePath().normalize();
        for (String sourcePath : sourcePaths) {
            var source = Paths.get(sourcePath).normalize();
            if (Files.notExists(source)) {
                throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
            }

            if (Files.isRegularFile(source)) {
                putSourcePath(collectedPaths, releaseFilePath, source);
            } else if (Files.isDirectory(source)) {
                try (var paths = Files.walk(source)) {
                    paths.sorted(Comparator.naturalOrder())
                         .forEach(path -> putSourcePath(collectedPaths, releaseFilePath, path));
                }
            } else {
                throw new IllegalArgumentException("Source path is not a regular file or directory: " + sourcePath);
            }
        }
        return collectedPaths.values().stream().toList();
    }

    private void putSourcePath(LinkedHashMap<Path, Path> collectedPaths, Path releaseFilePath, Path path) {
        var normalizedPath = path.normalize();
        var absolutePath = normalizedPath.toAbsolutePath().normalize();
        if (!absolutePath.equals(releaseFilePath)) {
            collectedPaths.putIfAbsent(absolutePath, normalizedPath);
        }
    }

    private void addDirectory(ZipOutputStream outputStream, Path directory) throws Exception {
        outputStream.putNextEntry(new ZipEntry(zipEntryName(directory) + "/"));
        outputStream.closeEntry();
    }

    private void addFile(ZipOutputStream outputStream, Path file) throws Exception {
        outputStream.putNextEntry(new ZipEntry(zipEntryName(file)));
        Files.copy(file, outputStream);
        outputStream.closeEntry();
    }

    private String zipEntryName(Path path) {
        var normalizedPath = path.normalize();
        var root = normalizedPath.getRoot();
        if (root != null) {
            normalizedPath = root.relativize(normalizedPath);
        }
        return normalizedPath.toString().replace("\\", "/");
    }
}

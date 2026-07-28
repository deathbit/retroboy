package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ReleaseComponentImpl implements ReleaseComponent {

    private static final int BUFFER_SIZE = 1024 * 1024;

    @Override
    public void release(Path targetPath, List<Path> sourcePaths, List<Path> rootFilePaths) {
        try {
            var releaseDir = targetPath.getParent();
            if (releaseDir != null) {
                Files.createDirectories(releaseDir);
            }
            var sources = collectSourceEntries(sourcePaths, rootFilePaths, targetPath);
            zip(targetPath, sources);
        } catch (Exception e) {
            throw new RuntimeException("Failed to release new package", e);
        }
    }

    private void zip(Path releaseFile, List<ReleaseEntry> sourcePaths) throws Exception {
        ProgressBar pb = new ProgressBar("发布新包");
        pb.startTask(sourcePaths.size());
        try (var outputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(
                releaseFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE))) {
            outputStream.setLevel(Deflater.BEST_SPEED);
            for (int i = 0; i < sourcePaths.size(); i++) {
                var entry = sourcePaths.get(i);
                var path = entry.path();
                if (Files.isDirectory(path)) {
                    addDirectory(outputStream, entry);
                } else if (Files.isRegularFile(path)) {
                    addFile(outputStream, entry);
                }
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        } finally {
            if (pb.getCurrentPercentage() < 1.0) {
                pb.close();
            }
        }
    }

    private List<ReleaseEntry> collectSourceEntries(List<Path> sourcePaths, List<Path> rootFilePaths, Path releaseFile) throws Exception {
        var collectedEntries = new LinkedHashMap<String, ReleaseEntry>();
        var releaseFilePath = releaseFile.toAbsolutePath().normalize();
        for (Path sourcePath : sourcePaths) {
            var source = sourcePath.normalize();
            if (Files.notExists(source)) {
                throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
            }

            if (Files.isRegularFile(source)) {
                putSourceEntry(collectedEntries, releaseFilePath, source, zipEntryName(source));
            } else if (Files.isDirectory(source)) {
                try (var paths = Files.walk(source)) {
                    paths.sorted(Comparator.naturalOrder())
                         .forEach(path -> putSourceEntry(collectedEntries, releaseFilePath, path, zipEntryName(path)));
                }
            } else {
                throw new IllegalArgumentException("Source path is not a regular file or directory: " + sourcePath);
            }
        }

        for (Path rootFilePath : rootFilePaths == null ? Collections.<Path>emptyList() : rootFilePaths) {
            var rootFile = rootFilePath.normalize();
            if (Files.notExists(rootFile)) {
                throw new IllegalArgumentException("Root file path does not exist: " + rootFilePath);
            }
            if (!Files.isRegularFile(rootFile)) {
                throw new IllegalArgumentException("Root file path is not a regular file: " + rootFilePath);
            }
            putSourceEntry(collectedEntries, releaseFilePath, rootFile, rootFile.getFileName().toString());
        }
        return collectedEntries.values().stream().toList();
    }

    private void putSourceEntry(LinkedHashMap<String, ReleaseEntry> collectedEntries,
                                Path releaseFilePath,
                                Path path,
                                String entryName) {
        var normalizedPath = path.normalize();
        var absolutePath = normalizedPath.toAbsolutePath().normalize();
        if (!absolutePath.equals(releaseFilePath)) {
            collectedEntries.putIfAbsent(entryName, new ReleaseEntry(normalizedPath, entryName));
        }
    }

    private void addDirectory(ZipOutputStream outputStream, ReleaseEntry entry) throws Exception {
        outputStream.putNextEntry(new ZipEntry(entry.entryName() + "/"));
        outputStream.closeEntry();
    }

    private void addFile(ZipOutputStream outputStream, ReleaseEntry entry) throws Exception {
        outputStream.putNextEntry(new ZipEntry(entry.entryName()));
        Files.copy(entry.path(), outputStream);
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

    private record ReleaseEntry(Path path, String entryName) {
    }
}

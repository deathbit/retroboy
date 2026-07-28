package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FileComponentImpl implements FileComponent {

    @Override
    public void deletePath(Path path) {
        try {
            if (Files.notExists(path)) {
                return;
            }

            if (Files.isDirectory(path)) {
                ProgressBar pb = new ProgressBar("删除路径");
                try (Stream<Path> walk = Files.walk(path)) {
                    List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
                    pb.startTask(paths.size());
                    for (int i = 0; i < paths.size(); i++) {
                        Files.delete(paths.get(i));
                        pb.updateTask(i);
                    }
                    pb.finishTaskAndClose();
                }
            } else {
                Files.delete(path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copyPath(PathPair pathPair) {
        try {
            Path sourcePathObj = pathPair.getSourcePath();
            Path targetDir = pathPair.getTargetPath();
            Files.createDirectories(targetDir);
            if (Files.isDirectory(sourcePathObj)) {
                ProgressBar pb = new ProgressBar("拷贝路径");
                Path targetRoot = targetDir.resolve(sourcePathObj.getFileName());
                try (Stream<Path> walk = Files.walk(sourcePathObj)) {
                    List<Path> paths = walk.toList();
                    pb.startTask(paths.size());
                    for (int i = 0; i < paths.size(); i++) {
                        Path currentSource = paths.get(i);
                        Path currentTarget = targetRoot.resolve(sourcePathObj.relativize(currentSource));
                        if (Files.isDirectory(currentSource)) {
                            Files.createDirectories(currentTarget);
                        } else {
                            Files.copy(currentSource, currentTarget, StandardCopyOption.REPLACE_EXISTING);
                        }
                        pb.updateTask(i);
                    }
                    pb.finishTaskAndClose();
                }
            } else {
                Files.copy(sourcePathObj, targetDir.resolve(sourcePathObj.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rename(Path sourcePath, String newName) {
        try {
            Files.move(sourcePath, sourcePath.resolveSibling(newName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
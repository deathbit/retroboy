package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FileComponentImpl implements FileComponent {

    @Override
    public void deletePath(String path) {
        try {
            Path pathObj = Paths.get(path);
            if (Files.notExists(pathObj)) {
                return;
            }

            if (Files.isDirectory(pathObj)) {
                ProgressBar pb = new ProgressBar("删除路径");
                try (Stream<Path> walk = Files.walk(pathObj)) {
                    List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
                    pb.startTask(paths.size());
                    for (int i = 0; i < paths.size(); i++) {
                        Files.delete(paths.get(i));
                        pb.updateTask(i);
                    }
                    pb.finishTaskAndClose();
                }
            } else {
                Files.delete(pathObj);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copyPath(PathPair pathPair) {
        try {
            Path sourcePathObj = Paths.get(pathPair.getSourcePath());
            Path targetDir = Paths.get(pathPair.getTargetPath());
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
    public void rename(String sourcePath, String newName) {
        try {
            Path sourcePathObj = Paths.get(sourcePath);
            Files.move(sourcePathObj, sourcePathObj.resolveSibling(newName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
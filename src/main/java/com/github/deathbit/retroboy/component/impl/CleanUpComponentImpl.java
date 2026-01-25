package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Component
public class CleanUpComponentImpl implements CleanUpComponent {

    @Override
    public void deleteDir(String dir) {
        if (dir == null || dir.trim().isEmpty()) {
            log.warn("Directory path is null or empty, skipping deletion");
            return;
        }

        Path dirPath = Paths.get(dir);
        
        if (!Files.exists(dirPath)) {
            log.info("Directory does not exist: {}", dir);
            return;
        }

        if (!Files.isDirectory(dirPath)) {
            log.warn("Path is not a directory: {}", dir);
            return;
        }

        try {
            // Delete directory recursively by walking the file tree in reverse order
            try (Stream<Path> walk = Files.walk(dirPath)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted: {}", path);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", path, e);
                        }
                    });
            }
            log.info("Successfully deleted directory: {}", dir);
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", dir, e);
        }
    }

    @Override
    public void deleteFile(String file) {
        if (file == null || file.trim().isEmpty()) {
            log.warn("File path is null or empty, skipping deletion");
            return;
        }

        Path filePath = Paths.get(file);
        
        if (!Files.exists(filePath)) {
            log.info("File does not exist: {}", file);
            return;
        }

        if (Files.isDirectory(filePath)) {
            log.warn("Path is a directory, not a file: {}", file);
            return;
        }

        try {
            Files.delete(filePath);
            log.info("Successfully deleted file: {}", file);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", file, e);
        }
    }
}

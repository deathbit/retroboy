package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class CleanUpComponentImpl implements CleanUpComponent {
    
    private final ProgressBarComponent progressBarComponent;
    
    public CleanUpComponentImpl(ProgressBarComponent progressBarComponent) {
        this.progressBarComponent = progressBarComponent;
    }

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
                List<Path> failedPaths = new ArrayList<>();
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted: {}", path);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", path, e);
                            failedPaths.add(path);
                        }
                    });
                
                if (!failedPaths.isEmpty()) {
                    log.warn("Directory deletion completed with {} failure(s). Failed paths: {}", 
                             failedPaths.size(), failedPaths);
                } else {
                    log.info("Successfully deleted directory: {}", dir);
                }
            }
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

    @Override
    public void batchDeleteDir(List<String> dirs) {
        if (dirs == null || dirs.isEmpty()) {
            log.info("No directories to delete");
            return;
        }
        
        progressBarComponent.start("Batch Delete Directories", dirs.size());
        
        for (String dir : dirs) {
            deleteDir(dir);
            progressBarComponent.update(dir);
        }
        
        progressBarComponent.finish();
    }

    @Override
    public void batchDeleteFile(List<String> files) {
        if (files == null || files.isEmpty()) {
            log.info("No files to delete");
            return;
        }
        
        progressBarComponent.start("Batch Delete Files", files.size());
        
        for (String file : files) {
            deleteFile(file);
            progressBarComponent.update(file);
        }
        
        progressBarComponent.finish();
    }

    @Override
    public void cleanupDir(String dir) {

    }

    @Override
    public void batchCleanupDir(String dir) {

    }
}

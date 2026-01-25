package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CleanUpComponentImpl implements CleanUpComponent {
    
    private final ProgressBarComponent progressBarComponent;
    
    public CleanUpComponentImpl(ProgressBarComponent progressBarComponent) {
        this.progressBarComponent = progressBarComponent;
    }

    @Override
    public void deleteDir(String dir) {
        if (dir == null || dir.trim().isEmpty()) {
            System.out.println("Directory path is null or empty, skipping deletion");
            return;
        }

        Path dirPath = Paths.get(dir);
        
        if (!Files.exists(dirPath)) {
            System.out.println("Directory does not exist: " + dir);
            return;
        }

        if (!Files.isDirectory(dirPath)) {
            System.out.println("Path is not a directory: " + dir);
            return;
        }

        try {
            // Delete directory recursively by walking the file tree in reverse order
            try (Stream<Path> walk = Files.walk(dirPath)) {
                List<Path> failedPaths = Collections.synchronizedList(new ArrayList<>());
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Deleted: " + path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                            failedPaths.add(path);
                        }
                    });
                
                if (!failedPaths.isEmpty()) {
                    System.out.println("Directory deletion completed with " + failedPaths.size() + 
                                      " failure(s). Failed paths: " + failedPaths);
                } else {
                    System.out.println("Successfully deleted directory: " + dir);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to delete directory: " + dir + " - " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String file) {
        if (file == null || file.trim().isEmpty()) {
            System.out.println("File path is null or empty, skipping deletion");
            return;
        }

        Path filePath = Paths.get(file);
        
        if (!Files.exists(filePath)) {
            return;
        }

        if (Files.isDirectory(filePath)) {
            System.out.println("Path is a directory, not a file: " + file);
            return;
        }

        try {
            Files.delete(filePath);
            System.out.println("Successfully deleted file: " + file);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + file + " - " + e.getMessage());
        }
    }

    @Override
    public void batchDeleteDir(List<String> dirs) {
        if (dirs == null || dirs.isEmpty()) {
            System.out.println("No directories to delete");
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
            System.out.println("No files to delete");
            return;
        }
        
        progressBarComponent.start("Batch Delete Files", files.size());
        
        for (String file : files) {
            deleteFile(file);
            progressBarComponent.update("删除文件：" + file);
        }
        
        progressBarComponent.finish();
    }

    @Override
    public void cleanupDir(String dir) {
        if (dir == null || dir.trim().isEmpty()) {
            System.out.println("Directory path is null or empty, skipping cleanup");
            return;
        }

        Path dirPath = Paths.get(dir);
        
        if (!Files.exists(dirPath)) {
            System.out.println("Directory does not exist: " + dir);
            return;
        }

        if (!Files.isDirectory(dirPath)) {
            System.out.println("Path is not a directory: " + dir);
            return;
        }

        try {
            // Delete directory contents but not the directory itself
            try (Stream<Path> walk = Files.walk(dirPath)) {
                List<Path> failedPaths = Collections.synchronizedList(new ArrayList<>());
                walk.filter(path -> !path.equals(dirPath)) // Skip the directory itself
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                            failedPaths.add(path);
                        }
                    });
                if (!failedPaths.isEmpty()) {
                    System.out.println("Directory cleanup completed with " + failedPaths.size() + 
                                      " failure(s). Failed paths: " + failedPaths);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to cleanup directory: " + dir + " - " + e.getMessage());
        }
    }

    @Override
    public void batchCleanupDir(List<String> dirs) {
        if (dirs == null || dirs.isEmpty()) {
            System.out.println("No directories to cleanup");
            return;
        }
        
        progressBarComponent.start("Batch Cleanup Directories", dirs.size());
        
        for (String dir : dirs) {
            cleanupDir(dir);
            progressBarComponent.update("清理目录：" + dir);
        }
        
        progressBarComponent.finish();
    }
}

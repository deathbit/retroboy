package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CopyComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import com.github.deathbit.retroboy.config.domain.CopyDir;
import com.github.deathbit.retroboy.config.domain.CopyFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CopyComponentImpl implements CopyComponent {
    
    private final ProgressBarComponent progressBarComponent;
    
    public CopyComponentImpl(ProgressBarComponent progressBarComponent) {
        this.progressBarComponent = progressBarComponent;
    }

    @Override
    public void copyDirContent(CopyDir copyDir) {
        if (copyDir == null) {
            System.out.println("CopyDir is null, skipping copy");
            return;
        }
        
        String src = copyDir.getSrc();
        String dest = copyDir.getDest();
        
        if (src == null || src.trim().isEmpty()) {
            System.out.println("Source directory path is null or empty, skipping copy");
            return;
        }
        
        if (dest == null || dest.trim().isEmpty()) {
            System.out.println("Destination directory path is null or empty, skipping copy");
            return;
        }
        
        Path srcPath = Paths.get(src);
        Path destPath = Paths.get(dest);
        
        if (!Files.exists(srcPath)) {
            System.out.println("Source directory does not exist: " + src);
            return;
        }
        
        if (!Files.isDirectory(srcPath)) {
            System.out.println("Source path is not a directory: " + src);
            return;
        }
        
        try {
            // Create destination directory if it doesn't exist
            if (!Files.exists(destPath)) {
                Files.createDirectories(destPath);
            }
            
            // Copy all contents from source to destination
            try (Stream<Path> walk = Files.walk(srcPath)) {
                walk.forEach(source -> {
                    try {
                        Path destination = destPath.resolve(srcPath.relativize(source));
                        
                        if (Files.isDirectory(source)) {
                            // Create directory if it doesn't exist
                            if (!Files.exists(destination)) {
                                Files.createDirectories(destination);
                            }
                        } else {
                            // Copy file, overwriting if it exists
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to copy: " + source + " - " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Failed to copy directory content: " + e.getMessage());
        }
    }

    @Override
    public void batchCopyDirContent(List<CopyDir> copyDirs) {
        if (copyDirs == null || copyDirs.isEmpty()) {
            System.out.println("No directories to copy");
            return;
        }
        
        progressBarComponent.start("Batch Copy Directories", copyDirs.size());
        
        for (CopyDir copyDir : copyDirs) {
            copyDirContent(copyDir);
            progressBarComponent.update("拷贝目录：" + copyDir.getSrc() + " -> " + copyDir.getDest());
        }
        
        progressBarComponent.finish();
    }

    @Override
    public void copyFile(CopyFile copyFile) {
        if (copyFile == null) {
            System.out.println("CopyFile is null, skipping copy");
            return;
        }
        
        String srcFile = copyFile.getSrcFile();
        String destDir = copyFile.getDestDir();
        
        if (srcFile == null || srcFile.trim().isEmpty()) {
            System.out.println("Source file path is null or empty, skipping copy");
            return;
        }
        
        if (destDir == null || destDir.trim().isEmpty()) {
            System.out.println("Destination directory path is null or empty, skipping copy");
            return;
        }
        
        Path srcPath = Paths.get(srcFile);
        Path destDirPath = Paths.get(destDir);
        
        if (!Files.exists(srcPath)) {
            System.out.println("Source file does not exist: " + srcFile);
            return;
        }
        
        if (Files.isDirectory(srcPath)) {
            System.out.println("Source path is a directory, not a file: " + srcFile);
            return;
        }
        
        try {
            // Create destination directory if it doesn't exist
            if (!Files.exists(destDirPath)) {
                Files.createDirectories(destDirPath);
            }
            
            // Get the file name from source path
            Path fileName = srcPath.getFileName();
            Path destFilePath = destDirPath.resolve(fileName);
            
            // Copy file, overwriting if it exists
            Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to copy file: " + e.getMessage());
        }
    }

    @Override
    public void batchCopyFile(List<CopyFile> copyFiles) {
        if (copyFiles == null || copyFiles.isEmpty()) {
            System.out.println("No files to copy");
            return;
        }
        
        progressBarComponent.start("Batch Copy Files", copyFiles.size());
        
        for (CopyFile copyFile : copyFiles) {
            copyFile(copyFile);
            progressBarComponent.update("拷贝文件：" + copyFile.getSrcFile() + " -> " + copyFile.getDestDir());
        }
        
        progressBarComponent.finish();
    }
}

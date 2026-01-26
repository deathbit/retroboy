package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FileComponentImpl implements FileComponent {

    private final ProgressBarComponent progressBarComponent;

    public FileComponentImpl(ProgressBarComponent progressBarComponent) {
        this.progressBarComponent = progressBarComponent;
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("File path is null or empty, skipping deletion");
            return;
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            System.out.println("File does not exist: " + filePath);
            return;
        }

        if (Files.isDirectory(path)) {
            System.out.println("Path is a directory, not a file: " + filePath);
            return;
        }

        try {
            Files.delete(path);
            System.out.println("Deleted file: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + filePath + " - " + e.getMessage());
        }
    }

    @Override
    public void deleteDir(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            System.out.println("Directory path is null or empty, skipping deletion");
            return;
        }

        Path path = Paths.get(dirPath);

        if (!Files.exists(path)) {
            System.out.println("Directory does not exist: " + dirPath);
            return;
        }

        if (!Files.isDirectory(path)) {
            System.out.println("Path is not a directory: " + dirPath);
            return;
        }

        try {
            // Delete directory recursively by walking the file tree in reverse order
            try (Stream<Path> walk = Files.walk(path)) {
                List<Path> failedPaths = Collections.synchronizedList(new ArrayList<>());
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            System.out.println("Deleted: " + p);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + p + " - " + e.getMessage());
                            failedPaths.add(p);
                        }
                    });

                if (!failedPaths.isEmpty()) {
                    System.out.println("Directory deletion completed with " + failedPaths.size() +
                                      " failure(s). Failed paths: " + failedPaths);
                } else {
                    System.out.println("Successfully deleted directory: " + dirPath);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to delete directory: " + dirPath + " - " + e.getMessage());
        }
    }

    @Override
    public void deleteDirContent(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            System.out.println("Directory path is null or empty, skipping cleanup");
            return;
        }

        Path path = Paths.get(dirPath);

        if (!Files.exists(path)) {
            System.out.println("Directory does not exist: " + dirPath);
            return;
        }

        if (!Files.isDirectory(path)) {
            System.out.println("Path is not a directory: " + dirPath);
            return;
        }

        try {
            // Delete directory contents but not the directory itself
            try (Stream<Path> walk = Files.walk(path)) {
                List<Path> failedPaths = Collections.synchronizedList(new ArrayList<>());
                walk.filter(p -> !p.equals(path)) // Skip the directory itself
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            System.out.println("Deleted: " + p);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + p + " - " + e.getMessage());
                            failedPaths.add(p);
                        }
                    });
                if (!failedPaths.isEmpty()) {
                    System.out.println("Directory content deletion completed with " + failedPaths.size() +
                                      " failure(s). Failed paths: " + failedPaths);
                } else {
                    System.out.println("Successfully deleted directory content: " + dirPath);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to delete directory content: " + dirPath + " - " + e.getMessage());
        }
    }

    @Override
    public void batchDeleteFile(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            System.out.println("No files to delete");
            return;
        }

        progressBarComponent.start("Batch Delete Files", filePaths.size());

        for (String filePath : filePaths) {
            deleteFile(filePath);
            progressBarComponent.update("Deleted file: " + filePath);
        }

        progressBarComponent.finish();
    }

    @Override
    public void batchDeleteDir(List<String> dirPaths) {
        if (dirPaths == null || dirPaths.isEmpty()) {
            System.out.println("No directories to delete");
            return;
        }

        progressBarComponent.start("Batch Delete Directories", dirPaths.size());

        for (String dirPath : dirPaths) {
            deleteDir(dirPath);
            progressBarComponent.update("Deleted directory: " + dirPath);
        }

        progressBarComponent.finish();
    }

    @Override
    public void batchDeleteDirContent(List<String> dirPaths) {
        if (dirPaths == null || dirPaths.isEmpty()) {
            System.out.println("No directory contents to delete");
            return;
        }

        progressBarComponent.start("Batch Delete Directory Contents", dirPaths.size());

        for (String dirPath : dirPaths) {
            deleteDirContent(dirPath);
            progressBarComponent.update("Deleted directory content: " + dirPath);
        }

        progressBarComponent.finish();
    }

    @Override
    public void createDir(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            System.out.println("Directory path is null or empty, skipping creation");
            return;
        }

        Path path = Paths.get(dirPath);

        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                System.out.println("Directory already exists: " + dirPath);
            } else {
                System.out.println("Path exists but is not a directory: " + dirPath);
            }
            return;
        }

        try {
            Files.createDirectories(path);
            System.out.println("Created directory: " + dirPath);
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + dirPath + " - " + e.getMessage());
        }
    }

    @Override
    public void batchCreateDir(List<String> dirPaths) {
        if (dirPaths == null || dirPaths.isEmpty()) {
            System.out.println("No directories to create");
            return;
        }

        progressBarComponent.start("Batch Create Directories", dirPaths.size());

        for (String dirPath : dirPaths) {
            createDir(dirPath);
            progressBarComponent.update("Created directory: " + dirPath);
        }

        progressBarComponent.finish();
    }

    @Override
    public void copyFile(CopyFileInput copyFileInput) {
        if (copyFileInput == null) {
            System.out.println("CopyFileInput is null, skipping copy");
            return;
        }

        String srcFilePath = copyFileInput.getSrcFilePath();
        String destDirPath = copyFileInput.getDestDirPath();

        if (srcFilePath == null || srcFilePath.trim().isEmpty()) {
            System.out.println("Source file path is null or empty, skipping copy");
            return;
        }

        if (destDirPath == null || destDirPath.trim().isEmpty()) {
            System.out.println("Destination directory path is null or empty, skipping copy");
            return;
        }

        Path srcPath = Paths.get(srcFilePath);
        Path destDirPathObj = Paths.get(destDirPath);

        if (!Files.exists(srcPath)) {
            System.out.println("Source file does not exist: " + srcFilePath);
            return;
        }

        if (Files.isDirectory(srcPath)) {
            System.out.println("Source path is a directory, not a file: " + srcFilePath);
            return;
        }

        try {
            // Create destination directory if it doesn't exist
            if (!Files.exists(destDirPathObj)) {
                Files.createDirectories(destDirPathObj);
            }

            // Get the file name from source path
            Path fileName = srcPath.getFileName();
            Path destFilePath = destDirPathObj.resolve(fileName);

            // Copy file, overwriting if it exists
            Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied file: " + srcFilePath + " -> " + destFilePath);
        } catch (IOException e) {
            System.err.println("Failed to copy file: " + srcFilePath + " - " + e.getMessage());
        }
    }

    @Override
    public void copyDir(CopyDirInput copyDirInput) {
        if (copyDirInput == null) {
            System.out.println("CopyDirInput is null, skipping copy");
            return;
        }

        String srcDirPath = copyDirInput.getSrcDirPath();
        String destDirPath = copyDirInput.getDestDirPath();

        if (srcDirPath == null || srcDirPath.trim().isEmpty()) {
            System.out.println("Source directory path is null or empty, skipping copy");
            return;
        }

        if (destDirPath == null || destDirPath.trim().isEmpty()) {
            System.out.println("Destination directory path is null or empty, skipping copy");
            return;
        }

        Path srcPath = Paths.get(srcDirPath);
        Path destPath = Paths.get(destDirPath);

        if (!Files.exists(srcPath)) {
            System.out.println("Source directory does not exist: " + srcDirPath);
            return;
        }

        if (!Files.isDirectory(srcPath)) {
            System.out.println("Source path is not a directory: " + srcDirPath);
            return;
        }

        try {
            // Get the name of the source directory
            String srcDirName = srcPath.getFileName().toString();

            // The target directory is destination + source directory name
            Path targetPath = destPath.resolve(srcDirName);

            // Create parent directories if they don't exist
            if (!Files.exists(destPath)) {
                Files.createDirectories(destPath);
            }

            // Create the target directory if it doesn't exist
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // Copy all contents from source to target
            try (Stream<Path> walk = Files.walk(srcPath)) {
                walk.forEach(source -> {
                    try {
                        Path destination = targetPath.resolve(srcPath.relativize(source));

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
            System.out.println("Copied directory: " + srcDirPath + " -> " + targetPath);
        } catch (IOException e) {
            System.err.println("Failed to copy directory: " + srcDirPath + " - " + e.getMessage());
        }
    }

    @Override
    public void copyDirContent(CopyDirContentInput copyDirContentInput) {
        if (copyDirContentInput == null) {
            System.out.println("CopyDirContentInput is null, skipping copy");
            return;
        }

        String srcDirPath = copyDirContentInput.getSrcDirPath();
        String destDirPath = copyDirContentInput.getDestDirPath();

        if (srcDirPath == null || srcDirPath.trim().isEmpty()) {
            System.out.println("Source directory path is null or empty, skipping copy");
            return;
        }

        if (destDirPath == null || destDirPath.trim().isEmpty()) {
            System.out.println("Destination directory path is null or empty, skipping copy");
            return;
        }

        Path srcPath = Paths.get(srcDirPath);
        Path destPath = Paths.get(destDirPath);

        if (!Files.exists(srcPath)) {
            System.out.println("Source directory does not exist: " + srcDirPath);
            return;
        }

        if (!Files.isDirectory(srcPath)) {
            System.out.println("Source path is not a directory: " + srcDirPath);
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
            System.out.println("Copied directory content: " + srcDirPath + " -> " + destDirPath);
        } catch (IOException e) {
            System.err.println("Failed to copy directory content: " + srcDirPath + " - " + e.getMessage());
        }
    }

    @Override
    public void batchCopyFile(List<CopyFileInput> copyFileInputs) {
        if (copyFileInputs == null || copyFileInputs.isEmpty()) {
            System.out.println("No files to copy");
            return;
        }

        progressBarComponent.start("Batch Copy Files", copyFileInputs.size());

        for (CopyFileInput copyFileInput : copyFileInputs) {
            copyFile(copyFileInput);
            progressBarComponent.update("Copied file: " + copyFileInput.getSrcFilePath());
        }

        progressBarComponent.finish();
    }

    @Override
    public void batchCopyDir(List<CopyDirInput> copyDirInputs) {
        if (copyDirInputs == null || copyDirInputs.isEmpty()) {
            System.out.println("No directories to copy");
            return;
        }

        progressBarComponent.start("Batch Copy Directories", copyDirInputs.size());

        for (CopyDirInput copyDirInput : copyDirInputs) {
            copyDir(copyDirInput);
            progressBarComponent.update("Copied directory: " + copyDirInput.getSrcDirPath());
        }

        progressBarComponent.finish();
    }

    @Override
    public void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) {
        if (copyDirContentInputs == null || copyDirContentInputs.isEmpty()) {
            System.out.println("No directory contents to copy");
            return;
        }

        progressBarComponent.start("Batch Copy Directory Contents", copyDirContentInputs.size());

        for (CopyDirContentInput copyDirContentInput : copyDirContentInputs) {
            copyDirContent(copyDirContentInput);
            progressBarComponent.update("Copied directory content: " + copyDirContentInput.getSrcDirPath());
        }

        progressBarComponent.finish();
    }

    @Override
    public void renameFile(RenameFileInput renameFileInput) {
        if (renameFileInput == null) {
            System.out.println("RenameFileInput is null, skipping rename");
            return;
        }

        String srcFilePath = renameFileInput.getSrcFilePath();
        String newFileName = renameFileInput.getNewFileName();

        if (srcFilePath == null || srcFilePath.trim().isEmpty()) {
            System.out.println("Source file path is null or empty, skipping rename");
            return;
        }

        if (newFileName == null || newFileName.trim().isEmpty()) {
            System.out.println("New file name is null or empty, skipping rename");
            return;
        }

        Path srcPath = Paths.get(srcFilePath);

        if (!Files.exists(srcPath)) {
            System.out.println("Source file does not exist: " + srcFilePath);
            return;
        }

        try {
            // Get the parent directory of the source file
            Path parentDir = srcPath.getParent();
            if (parentDir == null) {
                parentDir = Paths.get(".");
            }

            // Create the new file path with the new name
            Path destPath = parentDir.resolve(newFileName);

            // Rename (move) the file
            Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Renamed file: " + srcFilePath + " -> " + destPath);
        } catch (IOException e) {
            System.err.println("Failed to rename file: " + srcFilePath + " - " + e.getMessage());
        }
    }

    @Override
    public void batchRenameFile(List<RenameFileInput> renameFileInputs) {
        if (renameFileInputs == null || renameFileInputs.isEmpty()) {
            System.out.println("No files to rename");
            return;
        }

        progressBarComponent.start("Batch Rename Files", renameFileInputs.size());

        for (RenameFileInput renameFileInput : renameFileInputs) {
            renameFile(renameFileInput);
            progressBarComponent.update("Renamed file: " + renameFileInput.getSrcFilePath());
        }

        progressBarComponent.finish();
    }
}

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
    public void deleteFile(String filePath) throws IOException {
        Files.delete(Paths.get(filePath));
    }

    @Override
    public void deleteDir(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path p : paths) {
                Files.delete(p);
            }
        }
    }

    @Override
    public void deleteDirContent(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.filter(p -> !p.equals(path))
                .sorted(Comparator.reverseOrder())
                .toList();
            for (Path p : paths) {
                Files.delete(p);
            }
        }
    }

    @Override
    public void batchDeleteFile(List<String> filePaths) throws IOException {
        progressBarComponent.start("Batch Delete Files", filePaths.size());
        for (String filePath : filePaths) {
            deleteFile(filePath);
            progressBarComponent.update("Deleted file: " + filePath);
        }
        progressBarComponent.finish();
    }

    @Override
    public void batchDeleteDir(List<String> dirPaths) throws IOException {
        progressBarComponent.start("Batch Delete Directories", dirPaths.size());
        for (String dirPath : dirPaths) {
            deleteDir(dirPath);
            progressBarComponent.update("Deleted directory: " + dirPath);
        }
        progressBarComponent.finish();
    }

    @Override
    public void batchDeleteDirContent(List<String> dirPaths) throws IOException {
        progressBarComponent.start("Batch Delete Directory Contents", dirPaths.size());
        for (String dirPath : dirPaths) {
            deleteDirContent(dirPath);
            progressBarComponent.update("Deleted directory content: " + dirPath);
        }
        progressBarComponent.finish();
    }

    @Override
    public void createDir(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }

    @Override
    public void batchCreateDir(List<String> dirPaths) throws IOException {
        progressBarComponent.start("Batch Create Directories", dirPaths.size());
        for (String dirPath : dirPaths) {
            createDir(dirPath);
            progressBarComponent.update("Created directory: " + dirPath);
        }
        progressBarComponent.finish();
    }

    @Override
    public void copyFile(CopyFileInput copyFileInput) throws IOException {
        Path srcPath = Paths.get(copyFileInput.getSrcFilePath());
        Path destDirPath = Paths.get(copyFileInput.getDestDirPath());
        Files.createDirectories(destDirPath);
        Path destFilePath = destDirPath.resolve(srcPath.getFileName());
        Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void copyDir(CopyDirInput copyDirInput) throws IOException {
        Path srcPath = Paths.get(copyDirInput.getSrcDirPath());
        Path destPath = Paths.get(copyDirInput.getDestDirPath());
        Path targetPath = destPath.resolve(srcPath.getFileName());
        
        Files.createDirectories(targetPath);
        
        try (Stream<Path> walk = Files.walk(srcPath)) {
            List<Path> sources = walk.toList();
            for (Path source : sources) {
                Path destination = targetPath.resolve(srcPath.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @Override
    public void copyDirContent(CopyDirContentInput copyDirContentInput) throws IOException {
        Path srcPath = Paths.get(copyDirContentInput.getSrcDirPath());
        Path destPath = Paths.get(copyDirContentInput.getDestDirPath());
        
        Files.createDirectories(destPath);
        
        try (Stream<Path> walk = Files.walk(srcPath)) {
            List<Path> sources = walk.filter(source -> !source.equals(srcPath)).toList();
            for (Path source : sources) {
                Path destination = destPath.resolve(srcPath.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @Override
    public void batchCopyFile(List<CopyFileInput> copyFileInputs) throws IOException {
        progressBarComponent.start("Batch Copy Files", copyFileInputs.size());
        for (CopyFileInput copyFileInput : copyFileInputs) {
            copyFile(copyFileInput);
            progressBarComponent.update("Copied file: " + copyFileInput.getSrcFilePath());
        }
        progressBarComponent.finish();
    }

    @Override
    public void batchCopyDir(List<CopyDirInput> copyDirInputs) throws IOException {
        progressBarComponent.start("Batch Copy Directories", copyDirInputs.size());
        for (CopyDirInput copyDirInput : copyDirInputs) {
            copyDir(copyDirInput);
            progressBarComponent.update("Copied directory: " + copyDirInput.getSrcDirPath());
        }
        progressBarComponent.finish();
    }

    @Override
    public void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws IOException {
        progressBarComponent.start("Batch Copy Directory Contents", copyDirContentInputs.size());
        for (CopyDirContentInput copyDirContentInput : copyDirContentInputs) {
            copyDirContent(copyDirContentInput);
            progressBarComponent.update("Copied directory content: " + copyDirContentInput.getSrcDirPath());
        }
        progressBarComponent.finish();
    }

    @Override
    public void renameFile(RenameFileInput renameFileInput) throws IOException {
        Path srcPath = Paths.get(renameFileInput.getSrcFilePath());
        Path parentDir = srcPath.getParent();
        if (parentDir == null) {
            parentDir = Paths.get(".");
        }
        Path destPath = parentDir.resolve(renameFileInput.getNewFileName());
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void batchRenameFile(List<RenameFileInput> renameFileInputs) throws IOException {
        progressBarComponent.start("Batch Rename Files", renameFileInputs.size());
        for (RenameFileInput renameFileInput : renameFileInputs) {
            renameFile(renameFileInput);
            progressBarComponent.update("Renamed file: " + renameFileInput.getSrcFilePath());
        }
        progressBarComponent.finish();
    }
}

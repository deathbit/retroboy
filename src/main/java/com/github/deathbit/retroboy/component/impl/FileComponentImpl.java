package com.github.deathbit.retroboy.component.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import com.github.deathbit.retroboy.utils.Utils;
import org.springframework.stereotype.Component;

@Component
public class FileComponentImpl implements FileComponent {

    private void deleteFile(Path file) throws Exception {
        System.out.println("删除文件: " + file);
        Files.deleteIfExists(file);
    }

    private void deleteDir(Path dir) throws Exception {
        System.out.println("删除目录: " + dir);
        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (int i = 0; i < paths.size(); i++) {
                Files.delete(paths.get(i));
                Utils.printProgressBar(i, paths.size());
            }
        }
    }

    private void deleteDirContent(Path dir) throws Exception {
        System.out.println("清空目录: " + dir);
        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> paths = walk.filter(p -> !p.equals(dir))
                .sorted(Comparator.reverseOrder())
                .toList();
            for (int i = 0; i < paths.size(); i++) {
                Files.delete(paths.get(i));
                Utils.printProgressBar(i, paths.size());
            }
        }
    }

    @Override
    public void batchDeleteFile(List<Path> files) throws Exception {
        for (Path filePath : files) {
            deleteFile(filePath);
        }
    }

    @Override
    public void batchDeleteDir(List<Path> dirs) throws Exception {
        for (Path dirPath : dirs) {
            deleteDir(dirPath);
        }
    }

    @Override
    public void batchDeleteDirContent(List<Path> dirs) throws Exception {
        for (Path dirPath : dirs) {
            deleteDirContent(dirPath);
        }
    }

    private void createDir(Path dir) throws Exception {
        System.out.println("创建目录: " + dir);
        Files.createDirectories(dir);
    }

    @Override
    public void batchCreateDir(List<Path> dirs) throws Exception {
        for (Path dirPath : dirs) {
            createDir(dirPath);
        }
    }

    private void copyFile(CopyFileInput copyFileInput) throws Exception {
        Path srcPath = copyFileInput.getSrcFile();
        Path destDirPath = copyFileInput.getDestDir();
        System.out.println("拷贝文件: " + srcPath + " -> " + destDirPath);
        Files.createDirectories(destDirPath);
        Path destFilePath = destDirPath.resolve(srcPath.getFileName());
        Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyDir(CopyDirInput copyDirInput) throws Exception {
        Path srcPath = copyDirInput.getSrcDir();
        Path destPath = copyDirInput.getDestDir();
        System.out.println("拷贝目录: " + srcPath + " -> " + destPath);
        Path targetPath = destPath.resolve(srcPath.getFileName());

        Files.createDirectories(targetPath);

        try (Stream<Path> walk = Files.walk(srcPath)) {
            List<Path> sources = walk.toList();
            for (int i = 0; i < sources.size(); i++) {
                Path source = sources.get(i);
                Path destination = targetPath.resolve(srcPath.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
                Utils.printProgressBar(i, sources.size());
            }
        }
    }

    private void copyDirContent(CopyDirContentInput copyDirContentInput) throws Exception {
        Path srcPath = copyDirContentInput.getSrcDir();
        Path destPath = copyDirContentInput.getDestDir();
        System.out.println("拷贝目录内容: " + srcPath + " -> " + destPath);

        Files.createDirectories(destPath);

        try (Stream<Path> walk = Files.walk(srcPath)) {
            List<Path> sources = walk.filter(source -> !source.equals(srcPath)).toList();
            for (int i = 0; i < sources.size(); i++) {
                Path source = sources.get(i);
                Path destination = destPath.resolve(srcPath.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
                Utils.printProgressBar(i, sources.size());
            }
        }
    }

    @Override
    public void batchCopyFile(List<CopyFileInput> copyFileInputs) throws Exception {
        for (CopyFileInput copyFileInput : copyFileInputs) {
            copyFile(copyFileInput);
        }
    }

    @Override
    public void batchCopyDir(List<CopyDirInput> copyDirInputs) throws Exception {
        for (CopyDirInput copyDirInput : copyDirInputs) {
            copyDir(copyDirInput);
        }
    }

    @Override
    public void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws Exception {
        for (CopyDirContentInput copyDirContentInput : copyDirContentInputs) {
            copyDirContent(copyDirContentInput);
        }
    }

    private void renameFile(RenameFileInput renameFileInput) throws Exception {
        Path srcPath = renameFileInput.getSrcFile();
        Path parentDir = srcPath.getParent();
        if (parentDir == null) {
            parentDir = Paths.get(".");
        }
        Path destPath = parentDir.resolve(renameFileInput.getNewName());
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void batchRenameFile(List<RenameFileInput> renameFileInputs) throws Exception {
        System.out.println("批量重命名文件: ");
        for (int i = 0; i < renameFileInputs.size(); i++) {
            renameFile(renameFileInputs.get(i));
            Utils.printProgressBar(i, renameFileInputs.size());
        }
    }
}

package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import com.github.deathbit.retroboy.utils.Utils;
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

    @Override
    public void deleteFile(String file) throws IOException {
        System.out.println("删除文件: " + file);
        Files.deleteIfExists(Paths.get(file));
    }

    @Override
    public void deleteDir(String dir) throws IOException {
        System.out.println("删除目录: " + dir);
        Path path = Paths.get(dir);
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (int i = 0; i < paths.size(); i++) {
                Files.delete(paths.get(i));
                Utils.printProgressBar(i, paths.size());
            }
        }
    }

    @Override
    public void deleteDirContent(String dir) throws IOException {
        System.out.println("清空目录: " + dir);
        Path path = Paths.get(dir);
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.filter(p -> !p.equals(path))
                .sorted(Comparator.reverseOrder())
                .toList();
            for (int i = 0; i < paths.size(); i++) {
                Files.delete(paths.get(i));
                Utils.printProgressBar(i, paths.size());
            }
        }
    }

    @Override
    public void batchDeleteFile(List<String> files) throws IOException {
        for (String filePath : files) {
            deleteFile(filePath);
        }
    }

    @Override
    public void batchDeleteDir(List<String> dirs) throws IOException {
        for (String dirPath : dirs) {
            deleteDir(dirPath);
        }
    }

    @Override
    public void batchDeleteDirContent(List<String> dirs) throws IOException {
        for (String dirPath : dirs) {
            deleteDirContent(dirPath);
        }
    }

    @Override
    public void createDir(String dir) throws IOException {
        System.out.println("创建目录: " + dir);
        Files.createDirectories(Paths.get(dir));
    }

    @Override
    public void batchCreateDir(List<String> dirs) throws IOException {
        for (String dirPath : dirs) {
            createDir(dirPath);
        }
    }

    @Override
    public void copyFile(CopyFileInput copyFileInput) throws IOException {
        System.out.println("拷贝文件: " + copyFileInput.getSrcFile() + " -> " + copyFileInput.getDestDir());
        Path srcPath = Paths.get(copyFileInput.getSrcFile());
        Path destDirPath = Paths.get(copyFileInput.getDestDir());
        Files.createDirectories(destDirPath);
        Path destFilePath = destDirPath.resolve(srcPath.getFileName());
        Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void copyDir(CopyDirInput copyDirInput) throws IOException {
        System.out.println("拷贝目录: " + copyDirInput.getSrcDir() + " -> " + copyDirInput.getDestDir());
        Path srcPath = Paths.get(copyDirInput.getSrcDir());
        Path destPath = Paths.get(copyDirInput.getDestDir());
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

    @Override
    public void copyDirContent(CopyDirContentInput copyDirContentInput) throws IOException {
        System.out.println("拷贝目录内容: " + copyDirContentInput.getSrcDir() + " -> " + copyDirContentInput.getDestDir());
        Path srcPath = Paths.get(copyDirContentInput.getSrcDir());
        Path destPath = Paths.get(copyDirContentInput.getDestDir());

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
    public void batchCopyFile(List<CopyFileInput> copyFileInputs) throws IOException {
        for (CopyFileInput copyFileInput : copyFileInputs) {
            copyFile(copyFileInput);
        }
    }

    @Override
    public void batchCopyDir(List<CopyDirInput> copyDirInputs) throws IOException {
        for (CopyDirInput copyDirInput : copyDirInputs) {
            copyDir(copyDirInput);
        }
    }

    @Override
    public void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws IOException {
        for (CopyDirContentInput copyDirContentInput : copyDirContentInputs) {
            copyDirContent(copyDirContentInput);
        }
    }

    @Override
    public void renameFile(RenameFileInput renameFileInput) throws IOException {
        Path srcPath = Paths.get(renameFileInput.getSrcFile());
        Path parentDir = srcPath.getParent();
        if (parentDir == null) {
            parentDir = Paths.get(".");
        }
        Path destPath = parentDir.resolve(renameFileInput.getNewName());
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void batchRenameFile(List<RenameFileInput> renameFileInputs) throws IOException {
        System.out.println("批量重命名文件: ");
        for (int i = 0; i < renameFileInputs.size(); i++) {
            renameFile(renameFileInputs.get(i));
            Utils.printProgressBar(i, renameFileInputs.size());
        }
    }
}

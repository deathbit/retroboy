package com.github.deathbit.retroboy.component.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.CopyDirContentsInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import org.springframework.stereotype.Component;

@Component
public class FileComponentImpl implements FileComponent {

    @Override
    public void batchDeleteFiles(List<Path> files) throws Exception {
        ProgressBar pb = new ProgressBar("删除文件");
        pb.startTask(files.size());
        for (int i = 0; i < files.size(); i++) {
            Files.deleteIfExists(files.get(i));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchDeleteDirs(List<Path> dirs) throws Exception {
        ProgressBar pb = new ProgressBar("删除目录", dirs.size());
        for (Path dir : dirs) {
            try (Stream<Path> walk = Files.walk(dir)) {
                List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
                pb.startTask(paths.size());
                for (int i = 0; i < paths.size(); i++) {
                    Files.delete(paths.get(i));
                    pb.updateTask(i);
                }
                pb.finishTask();
            }
        }
        pb.close();
    }

    @Override
    public void batchCleanDirs(List<Path> dirs) throws Exception {
        ProgressBar pb = new ProgressBar("清空目录", dirs.size());
        for (Path dir : dirs) {
            try (Stream<Path> walk = Files.walk(dir)) {
                List<Path> paths = walk
                    .filter(p -> !p.equals(dir))
                    .sorted(Comparator.reverseOrder())
                    .toList();
                pb.startTask(paths.size());
                for (int i = 0; i < paths.size(); i++) {
                    Files.delete(paths.get(i));
                    pb.updateTask(i);
                }
                pb.finishTask();
            }
        }
        pb.close();
    }

    @Override
    public void batchCreateDirs(List<Path> dirs) throws Exception {
        ProgressBar pb = new ProgressBar("创建目录");
        pb.startTask(dirs.size());
        for (int i = 0; i < dirs.size(); i++) {
            Files.createDirectories(dirs.get(i));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchCopyFiles(List<CopyFileInput> copyFileInputs) throws Exception {
        ProgressBar pb = new ProgressBar("拷贝文件");
        pb.startTask(copyFileInputs.size());
        for (int i = 0; i < copyFileInputs.size(); i++) {
            CopyFileInput input = copyFileInputs.get(i);
            Path srcPath = input.getSrcFile();
            Path destDirPath = input.getDestDir();
            Files.createDirectories(destDirPath);
            Path destFilePath = destDirPath.resolve(srcPath.getFileName());
            Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchCopyDirs(List<CopyDirInput> copyDirInputs) throws Exception {
        ProgressBar pb = new ProgressBar("拷贝目录", copyDirInputs.size());
        for (CopyDirInput input : copyDirInputs) {
            Path srcPath = input.getSrcDir();
            Path destPath = input.getDestDir();
            Path targetPath = destPath.resolve(srcPath.getFileName());
            Files.createDirectories(targetPath);
            try (Stream<Path> walk = Files.walk(srcPath)) {
                List<Path> sources = walk.toList();
                copy(pb, srcPath, targetPath, sources);
            }
        }
        pb.close();
    }

    @Override
    public void batchCopyDirContentsToDirs(List<CopyDirContentsInput> copyDirContentsInputs) throws Exception {
        ProgressBar pb = new ProgressBar("拷贝内容", copyDirContentsInputs.size());
        for (CopyDirContentsInput input : copyDirContentsInputs) {
            Path srcPath = input.getSrcDir();
            Path destPath = input.getDestDir();
            Files.createDirectories(destPath);
            try (Stream<Path> walk = Files.walk(srcPath)) {
                List<Path> sources = walk
                    .filter(source -> !source.equals(srcPath))
                    .toList();
                copy(pb, srcPath, destPath, sources);
            }
        }
        pb.close();
    }

    @Override
    public void batchRenameFiles(List<RenameFileInput> renameFileInputs) throws Exception {
        ProgressBar pb = new ProgressBar("批量命名");
        pb.startTask(renameFileInputs.size());
        for (int i = 0; i < renameFileInputs.size(); i++) {
            RenameFileInput input = renameFileInputs.get(i);
            Path srcPath = input.getSrcFile();
            Path parentDir = srcPath.getParent();
            if (parentDir == null) {
                parentDir = Paths.get(".");
            }
            Path destPath = parentDir.resolve(input.getNewName());
            Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    private void copy(ProgressBar pb, Path srcPath, Path targetPath, List<Path> sources) throws IOException {
        pb.startTask(sources.size());
        for (int i = 0; i < sources.size(); i++) {
            Path source = sources.get(i);
            Path destination = targetPath.resolve(srcPath.relativize(source));
            if (Files.isDirectory(source)) {
                Files.createDirectories(destination);
            } else {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            pb.updateTask(i);
        }
        pb.finishTask();
    }
}
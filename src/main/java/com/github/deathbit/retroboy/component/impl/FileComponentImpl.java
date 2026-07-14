package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.CopyDirContentsInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FileComponentImpl implements FileComponent {

    @Override
    public void deletePath(String path) throws Exception {
        ProgressBar pb = new ProgressBar("删除路径");
        Path pathObj = Paths.get(path);
        if (Files.notExists(pathObj)) {
            pb.done();
            return;
        }

        try (Stream<Path> walk = Files.walk(pathObj)) {
            List<Path> paths = walk.toList();
            pb.startTask(paths.size());
            for (int i = 0; i < paths.size(); i++) {
                Files.delete(paths.get(i));
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        }
    }

    @Override
    public void copyPath(String sourcePath, String targetPath) throws Exception {
        ProgressBar pb = new ProgressBar("拷贝路径");
        Path source = Paths.get(sourcePath);
        Path targetDir = Paths.get(targetPath);
        Files.createDirectories(targetDir);

        if (Files.isDirectory(source)) {
            Path targetRoot = targetDir.resolve(source.getFileName());
            try (Stream<Path> walk = Files.walk(source)) {
                List<Path> paths = walk.toList();
                pb.startTask(paths.size());
                for (int i = 0; i < paths.size(); i++) {
                    Path currentSource = paths.get(i);
                    Path currentTarget = targetRoot.resolve(source.relativize(currentSource));
                    if (Files.isDirectory(currentSource)) {
                        Files.createDirectories(currentTarget);
                    } else {
                        Files.copy(currentSource, currentTarget, StandardCopyOption.REPLACE_EXISTING);
                    }
                    pb.updateTask(i);
                }
                pb.finishTaskAndClose();
            }
            return;
        }

        pb.startTask(1);
        Files.copy(source, targetDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        pb.updateTask(0);
        pb.finishTaskAndClose();
    }

    @Override
    public void batchDeleteFiles(List<String> files) throws Exception {
        ProgressBar pb = new ProgressBar("删除文件");
        pb.startTask(files.size());
        for (int i = 0; i < files.size(); i++) {
            Files.deleteIfExists(Paths.get(files.get(i)));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchDeleteDirs(List<String> dirs) throws Exception {
        ProgressBar pb = new ProgressBar("删除目录");
        List<Path> paths = new ArrayList<>();
        for (String dir : dirs) {
            Path dirPath = Paths.get(dir);
            if (Files.notExists(dirPath)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(dirPath)) {
                paths.addAll(walk.toList());
            }
        }
        paths = paths.stream().distinct().sorted(Comparator.reverseOrder()).toList();
        pb.startTask(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            Files.delete(paths.get(i));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchCleanDirs(List<String> dirs) throws Exception {
        ProgressBar pb = new ProgressBar("清空目录");
        List<Path> paths = new ArrayList<>();
        for (String dir : dirs) {
            Path dirPath = Paths.get(dir);
            if (Files.notExists(dirPath)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(dirPath)) {
                paths.addAll(walk.filter(p -> !p.equals(dirPath)).toList());
            }
        }
        paths = paths.stream().distinct().sorted(Comparator.reverseOrder()).toList();
        pb.startTask(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            Files.delete(paths.get(i));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchCreateDirs(List<String> dirs) throws Exception {
        ProgressBar pb = new ProgressBar("创建目录");
        pb.startTask(dirs.size());
        for (int i = 0; i < dirs.size(); i++) {
            Files.createDirectories(Paths.get(dirs.get(i)));
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
            Path srcPath = Paths.get(input.getSrcFile());
            Path destDirPath = Paths.get(input.getDestDir());
            Files.createDirectories(destDirPath);
            Path destFilePath = destDirPath.resolve(srcPath.getFileName());
            Files.copy(srcPath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    @Override
    public void batchCopyDirs(List<CopyDirInput> copyDirInputs) throws Exception {
        ProgressBar pb = new ProgressBar("拷贝目录");
        List<CopyPath> copyPaths = new ArrayList<>();
        for (CopyDirInput input : copyDirInputs) {
            Path srcPath = Paths.get(input.getSrcDir());
            Path destPath = Paths.get(input.getDestDir());
            Path targetPath = destPath.resolve(srcPath.getFileName());
            Files.createDirectories(targetPath);
            try (Stream<Path> walk = Files.walk(srcPath)) {
                copyPaths.addAll(walk.map(source -> new CopyPath(source, targetPath.resolve(srcPath.relativize(source)))).toList());
            }
        }
        copy(pb, copyPaths);
    }

    @Override
    public void batchCopyDirContentsToDirs(List<CopyDirContentsInput> copyDirContentsInputs) throws Exception {
        ProgressBar pb = new ProgressBar("拷贝内容");
        List<CopyPath> copyPaths = new ArrayList<>();
        for (CopyDirContentsInput input : copyDirContentsInputs) {
            Path srcPath = Paths.get(input.getSrcDir());
            Path destPath = Paths.get(input.getDestDir());
            Files.createDirectories(destPath);
            try (Stream<Path> walk = Files.walk(srcPath)) {
                copyPaths.addAll(walk.filter(source -> !source.equals(srcPath))
                                     .map(source -> new CopyPath(source, destPath.resolve(srcPath.relativize(source))))
                                     .toList());
            }
        }
        copy(pb, copyPaths);
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

    private void copy(ProgressBar pb, List<CopyPath> copyPaths) throws IOException {
        pb.startTask(copyPaths.size());
        for (int i = 0; i < copyPaths.size(); i++) {
            CopyPath copyPath = copyPaths.get(i);
            Path source = copyPath.source();
            Path destination = copyPath.destination();
            if (Files.isDirectory(source)) {
                Files.createDirectories(destination);
            } else {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    private record CopyPath(Path source, Path destination) {
    }
}
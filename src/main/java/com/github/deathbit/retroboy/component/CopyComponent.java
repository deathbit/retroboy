package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.config.domain.CopyDir;
import com.github.deathbit.retroboy.config.domain.CopyFile;

import java.util.List;

public interface CopyComponent {
    void copyDirContent(CopyDir copyDir);
    void batchCopyDirContent(List<CopyDir> copyDirs);
    void copyFile(CopyFile copyFile);
    void batchCopyFile(List<CopyFile> copyFiles);
    void copyDir(CopyDir copyDir);
    void batchCopyDir(List<CopyDir> copyDirs);
}

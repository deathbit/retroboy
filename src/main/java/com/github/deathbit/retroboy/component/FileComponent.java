package com.github.deathbit.retroboy.component;

import java.nio.file.Path;
import java.util.List;

import com.github.deathbit.retroboy.component.domain.CopyDirContentsInput;
import com.github.deathbit.retroboy.component.domain.CopyDirInput;
import com.github.deathbit.retroboy.component.domain.CopyFileInput;
import com.github.deathbit.retroboy.component.domain.RenameFileInput;

public interface FileComponent {
    void batchDeleteFiles(List<Path> files) throws Exception;
    void batchDeleteDirs(List<Path> dirs) throws Exception;
    void batchCleanDirs(List<Path> dirs) throws Exception;
    void batchCreateDirs(List<Path> dirs) throws Exception;
    void batchCopyFiles(List<CopyFileInput> copyFileInputs) throws Exception;
    void batchCopyDirs(List<CopyDirInput> copyDirInputs) throws Exception;
    void batchCopyDirContentsToDirs(List<CopyDirContentsInput> copyDirContentsInputs) throws Exception;
    void batchRenameFiles(List<RenameFileInput> renameFileInputs) throws Exception;
}

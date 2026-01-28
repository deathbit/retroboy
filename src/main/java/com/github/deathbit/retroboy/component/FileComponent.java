package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.CopyDirContentsInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;

import java.util.List;

public interface FileComponent {
    void batchDeleteFiles(List<String> files) throws Exception;

    void batchDeleteDirs(List<String> dirs) throws Exception;

    void batchCleanDirs(List<String> dirs) throws Exception;

    void batchCreateDirs(List<String> dirs) throws Exception;

    void batchCopyFiles(List<CopyFileInput> copyFileInputs) throws Exception;

    void batchCopyDirs(List<CopyDirInput> copyDirInputs) throws Exception;

    void batchCopyDirContentsToDirs(List<CopyDirContentsInput> copyDirContentsInputs) throws Exception;

    void batchRenameFiles(List<RenameFileInput> renameFileInputs) throws Exception;
}

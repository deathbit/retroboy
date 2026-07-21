package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.*;

import java.util.List;

public interface FileComponent {

    void deletePath(String path);

    void copyPath(PathPair pathPair);

    void rename(String sourcePath, String newName);

    void batchDeleteFiles(List<String> files) throws Exception;

    void batchDeleteDirs(List<String> dirs) throws Exception;

    void batchCleanDirs(List<String> dirs) throws Exception;

    void batchCreateDirs(List<String> dirs) throws Exception;

    void batchCopyFiles(List<CopyFileInput> copyFileInputs) throws Exception;

    void batchCopyDirs(List<CopyDirInput> copyDirInputs) throws Exception;

    void batchCopyDirContentsToDirs(List<CopyDirContentsInput> copyDirContentsInputs) throws Exception;

    void batchRenameFiles(List<RenameFileInput> renameFileInputs) throws Exception;
}

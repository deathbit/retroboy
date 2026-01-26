package com.github.deathbit.retroboy.component;

import java.io.IOException;
import java.util.List;

import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;

public interface FileComponent {
    void deleteFile(String filePath) throws IOException;
    void deleteDir(String dirPath);
    void deleteDirContent(String dirPath);
    void batchDeleteFile(List<String> filePaths);
    void batchDeleteDir(List<String> dirPaths);
    void batchDeleteDirContent(List<String> dirPaths);

    void createDir(String dirPath);
    void batchCreateDir(List<String> dirPaths);

    void copyFile(CopyFileInput copyFileInput);
    void copyDir(CopyDirInput copyDirInput);
    void copyDirContent(CopyDirContentInput copyDirContentInput);
    void batchCopyFile(List<CopyFileInput> copyFileInputs);
    void batchCopyDir(List<CopyDirInput> copyDirInputs);
    void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs);

    void renameFile(RenameFileInput renameFileInput);
    void batchRenameFile(List<RenameFileInput> renameFileInputs);
}

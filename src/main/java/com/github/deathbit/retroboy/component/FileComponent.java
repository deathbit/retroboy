package com.github.deathbit.retroboy.component;

import java.io.IOException;
import java.util.List;

import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;

public interface FileComponent {
    void deleteFile(String filePath) throws IOException;
    void deleteDir(String dirPath) throws IOException;
    void deleteDirContent(String dirPath) throws IOException;
    void batchDeleteFile(List<String> filePaths) throws IOException;
    void batchDeleteDir(List<String> dirPaths) throws IOException;
    void batchDeleteDirContent(List<String> dirPaths) throws IOException;

    void createDir(String dirPath) throws IOException;
    void batchCreateDir(List<String> dirPaths) throws IOException;

    void copyFile(CopyFileInput copyFileInput) throws IOException;
    void copyDir(CopyDirInput copyDirInput) throws IOException;
    void copyDirContent(CopyDirContentInput copyDirContentInput) throws IOException;
    void batchCopyFile(List<CopyFileInput> copyFileInputs) throws IOException;
    void batchCopyDir(List<CopyDirInput> copyDirInputs) throws IOException;
    void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws IOException;

    void renameFile(RenameFileInput renameFileInput) throws IOException;
    void batchRenameFile(List<RenameFileInput> renameFileInputs) throws IOException;
}

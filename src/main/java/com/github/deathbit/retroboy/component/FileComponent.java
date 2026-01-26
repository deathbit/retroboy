package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;

import java.io.IOException;
import java.util.List;

public interface FileComponent {
    void deleteFile(String file) throws IOException;
    void deleteDir(String dir) throws IOException;
    void deleteDirContent(String dir) throws IOException;
    void batchDeleteFile(List<String> files) throws IOException;
    void batchDeleteDir(List<String> dirs) throws IOException;
    void batchDeleteDirContent(List<String> dirs) throws IOException;

    void createDir(String dir) throws IOException;
    void batchCreateDir(List<String> dirs) throws IOException;

    void copyFile(CopyFileInput copyFileInput) throws IOException;
    void copyDir(CopyDirInput copyDirInput) throws IOException;
    void copyDirContent(CopyDirContentInput copyDirContentInput) throws IOException;
    void batchCopyFile(List<CopyFileInput> copyFileInputs) throws IOException;
    void batchCopyDir(List<CopyDirInput> copyDirInputs) throws IOException;
    void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws IOException;

    void renameFile(RenameFileInput renameFileInput) throws IOException;
    void batchRenameFile(List<RenameFileInput> renameFileInputs) throws IOException;
}

package com.github.deathbit.retroboy.component;

import java.util.List;

import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;

public interface FileComponent {
    void deleteFile(String file) throws Exception;
    void deleteDir(String dir) throws Exception;
    void deleteDirContent(String dir) throws Exception;
    void batchDeleteFile(List<String> files) throws Exception;
    void batchDeleteDir(List<String> dirs) throws Exception;
    void batchDeleteDirContent(List<String> dirs) throws Exception;

    void createDir(String dir) throws Exception;
    void batchCreateDir(List<String> dirs) throws Exception;

    void copyFile(CopyFileInput copyFileInput) throws Exception;
    void copyDir(CopyDirInput copyDirInput) throws Exception;
    void copyDirContent(CopyDirContentInput copyDirContentInput) throws Exception;
    void batchCopyFile(List<CopyFileInput> copyFileInputs) throws Exception;
    void batchCopyDir(List<CopyDirInput> copyDirInputs) throws Exception;
    void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws Exception;

    void renameFile(RenameFileInput renameFileInput) throws Exception;
    void batchRenameFile(List<RenameFileInput> renameFileInputs) throws Exception;
}

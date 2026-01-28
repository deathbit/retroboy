package com.github.deathbit.retroboy.component;

import java.nio.file.Path;
import java.util.List;

import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RenameFileInput;

public interface FileComponent {
    void batchDeleteFile(List<Path> files) throws Exception;
    void batchDeleteDir(List<Path> dirs) throws Exception;
    void batchDeleteDirContent(List<Path> dirs) throws Exception;

    void batchCreateDir(List<Path> dirs) throws Exception;

    void batchCopyFile(List<CopyFileInput> copyFileInputs) throws Exception;
    void batchCopyDir(List<CopyDirInput> copyDirInputs) throws Exception;
    void batchCopyDirContent(List<CopyDirContentInput> copyDirContentInputs) throws Exception;

    void batchRenameFile(List<RenameFileInput> renameFileInputs) throws Exception;
}

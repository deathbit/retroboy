package com.github.deathbit.retroboy.config.domain;

import lombok.Data;

import java.util.List;

@Data
public class CopyDefault {
    private List<CopyDir> copyDirs;
    private List<CopyFile> copyFiles;
}

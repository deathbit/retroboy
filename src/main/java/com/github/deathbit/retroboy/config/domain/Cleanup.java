package com.github.deathbit.retroboy.config.domain;

import lombok.Data;

import java.util.List;

@Data
public class Cleanup {
    private List<String> cleanupDirs;
    private List<String> deleteDirs;
    private List<String> deleteFiles;
}

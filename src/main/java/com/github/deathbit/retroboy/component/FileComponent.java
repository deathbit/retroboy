package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.PathPair;

import java.nio.file.Path;

public interface FileComponent {
    void deletePath(Path path);
    void copyPath(PathPair pathPair);
    void rename(Path sourcePath, String newName);
}

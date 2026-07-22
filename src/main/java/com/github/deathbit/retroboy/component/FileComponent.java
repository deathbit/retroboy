package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.PathPair;

public interface FileComponent {
    void deletePath(String path);
    void copyPath(PathPair pathPair);
    void rename(String sourcePath, String newName);
}

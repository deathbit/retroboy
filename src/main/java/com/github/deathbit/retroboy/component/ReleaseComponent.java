package com.github.deathbit.retroboy.component;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public interface ReleaseComponent {
    default void release(Path targetPath, List<Path> sourcePaths) {
        release(targetPath, sourcePaths, Collections.emptyList());
    }

    void release(Path targetPath, List<Path> sourcePaths, List<Path> rootFilePaths);
}

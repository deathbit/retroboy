package com.github.deathbit.retroboy.component;

import java.util.List;

public interface ReleaseComponent {
    void releaseNew(String targetPath, List<String> sourcePaths);
}

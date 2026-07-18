package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.PathPair;

public interface ReleaseComponent {
    void release(String fileName, PathPair pathPair) throws Exception;
}

package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.domain.PlatformContext;

import java.nio.file.Path;

@FunctionalInterface
public interface PathSupplier {
    Path get(PlatformContext platformContext);
}

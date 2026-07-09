package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.Platform;

import java.util.EnumSet;

public final class PlatformTaskMask {
    private final EnumSet<Platform> enabledPlatforms;

    public PlatformTaskMask(EnumSet<Platform> enabledPlatforms) {
        this.enabledPlatforms = enabledPlatforms;
    }

    public boolean isEnabled(Platform platform) {
        return enabledPlatforms.contains(platform);
    }
}


package com.github.deathbit.retroboy.enums;

import java.util.EnumSet;

final class PlatformTaskMask {
    private final EnumSet<Platform> enabledPlatforms;

    PlatformTaskMask(EnumSet<Platform> enabledPlatforms) {
        this.enabledPlatforms = enabledPlatforms;
    }

    boolean isEnabled(Platform platform) {
        return enabledPlatforms.contains(platform);
    }
}


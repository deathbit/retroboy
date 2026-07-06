package com.github.deathbit.retroboy.enums;

import lombok.Getter;

@Getter
public enum StartupTask {
    CLEAN_UP(1L),
    DEFAULT_CONFIG(1L << 1),
    FIX_CHINESE_FONT(1L << 2),
    SET_MEGA_BEZEL_SHADER(1L << 3),
    SET_PLATFORM(1L << 4);

    private final long mask;

    StartupTask(long mask) {
        this.mask = mask;
    }
}

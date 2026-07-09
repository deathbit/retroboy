package com.github.deathbit.retroboy.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;

public enum Platform {
    NES,
    SNES,
    MD;

    private static final String VALID_PLATFORM_NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    public static boolean isEnabled(Platform platform, String platformTaskMask) {
        if (platformTaskMask == null || platformTaskMask.isBlank()) {
            return false;
        }

        return parsePlatformTaskMask(platformTaskMask.trim()).isEnabled(platform);
    }

    private static PlatformTaskMask parsePlatformTaskMask(String platformTaskMask) {
        EnumSet<Platform> enabledPlatforms = EnumSet.noneOf(Platform.class);
        EnumSet<Platform> disabledPlatforms = EnumSet.noneOf(Platform.class);

        Arrays.stream(platformTaskMask.split("\\|"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .forEach(token -> {
                    boolean disabled = token.startsWith("!");
                    Platform platform = parsePlatform(disabled ? token.substring(1).trim() : token);
                    if (disabled) {
                        disabledPlatforms.add(platform);
                    } else {
                        enabledPlatforms.add(platform);
                    }
                });

        enabledPlatforms.removeAll(disabledPlatforms);
        return new PlatformTaskMask(enabledPlatforms);
    }

    private static Platform parsePlatform(String value) {
        try {
            return Platform.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown platform: %s. Valid platforms are: %s".formatted(value, VALID_PLATFORM_NAMES),
                    exception
            );
        }
    }

}

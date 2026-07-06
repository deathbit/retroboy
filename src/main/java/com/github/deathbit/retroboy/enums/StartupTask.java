package com.github.deathbit.retroboy.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static boolean isEnabled(StartupTask startupTask, String startupTaskMask) {
        if (startupTaskMask == null || startupTaskMask.isBlank()) {
            return false;
        }

        String trimmedStartupTaskMask = startupTaskMask.trim();
        if (isNumeric(trimmedStartupTaskMask)) {
            return (Long.parseLong(trimmedStartupTaskMask) & startupTask.getMask()) != 0;
        }

        Set<String> disabledTasks = parseTasks(trimmedStartupTaskMask, true);
        if (disabledTasks.contains(startupTask.name())) {
            return false;
        }

        return parseTasks(trimmedStartupTaskMask, false).contains(startupTask.name());
    }

    private static Set<String> parseTasks(String startupTaskMask, boolean disabled) {
        return Arrays.stream(startupTaskMask.split("\\|"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .filter(token -> token.startsWith("!") == disabled)
                .map(token -> disabled ? token.substring(1).trim() : token)
                .filter(token -> !token.isBlank())
                .map(token -> token.toUpperCase(Locale.ROOT))
                .peek(StartupTask::valueOf)
                .collect(Collectors.toSet());
    }

    private static boolean isNumeric(String value) {
        return value.chars().allMatch(Character::isDigit);
    }
}

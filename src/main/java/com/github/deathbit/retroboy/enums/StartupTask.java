package com.github.deathbit.retroboy.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;

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

        return parseStartupTaskMask(trimmedStartupTaskMask).isEnabled(startupTask);
    }

    private static StartupTaskMask parseStartupTaskMask(String startupTaskMask) {
        EnumSet<StartupTask> enabledTasks = EnumSet.noneOf(StartupTask.class);
        EnumSet<StartupTask> disabledTasks = EnumSet.noneOf(StartupTask.class);

        Arrays.stream(startupTaskMask.split("\\|"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .forEach(token -> {
                    boolean disabled = token.startsWith("!");
                    StartupTask task = parseTask(disabled ? token.substring(1).trim() : token);
                    if (disabled) {
                        disabledTasks.add(task);
                    } else {
                        enabledTasks.add(task);
                    }
                });

        enabledTasks.removeAll(disabledTasks);
        return new StartupTaskMask(enabledTasks);
    }

    private static StartupTask parseTask(String value) {
        try {
            return StartupTask.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown startup task: " + value, exception);
        }
    }

    private static boolean isNumeric(String value) {
        return value.chars().allMatch(Character::isDigit);
    }

    private record StartupTaskMask(EnumSet<StartupTask> enabledTasks) {
        private boolean isEnabled(StartupTask startupTask) {
            return enabledTasks.contains(startupTask);
        }
    }
}

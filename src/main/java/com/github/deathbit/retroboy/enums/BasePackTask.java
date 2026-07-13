package com.github.deathbit.retroboy.enums;

import com.github.deathbit.retroboy.domain.StartupTaskMask;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
public enum BasePackTask {
    CLEAN_UP(1L),
    DEFAULT_CONFIG(1L << 1),
    FIX_CHINESE_FONT(1L << 2),
    SET_MEGA_BEZEL_SHADER(1L << 3),
    SET_PLATFORM(1L << 4);

    private static final String VALID_TASK_NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    private final long mask;

    BasePackTask(long mask) {
        this.mask = mask;
    }

    public static boolean isEnabled(BasePackTask basePackTask, String startupTaskMask) {
        if (startupTaskMask == null || startupTaskMask.isBlank()) {
            return false;
        }

        String trimmedStartupTaskMask = startupTaskMask.trim();
        if (isNumeric(trimmedStartupTaskMask)) {
            return (Long.parseLong(trimmedStartupTaskMask) & basePackTask.getMask()) != 0;
        }

        return parseStartupTaskMask(trimmedStartupTaskMask).isEnabled(basePackTask);
    }

    private static StartupTaskMask parseStartupTaskMask(String startupTaskMask) {
        EnumSet<BasePackTask> enabledTasks = EnumSet.noneOf(BasePackTask.class);
        EnumSet<BasePackTask> disabledTasks = EnumSet.noneOf(BasePackTask.class);

        Arrays.stream(startupTaskMask.split("\\|"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .forEach(token -> {
                    boolean disabled = token.startsWith("!");
                    BasePackTask task = parseTask(disabled ? token.substring(1).trim() : token);
                    if (disabled) {
                        disabledTasks.add(task);
                    } else {
                        enabledTasks.add(task);
                    }
                });

        enabledTasks.removeAll(disabledTasks);
        return new StartupTaskMask(enabledTasks);
    }

    private static BasePackTask parseTask(String value) {
        try {
            return BasePackTask.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown startup task: %s. Valid tasks are: %s".formatted(value, VALID_TASK_NAMES),
                    exception
            );
        }
    }

    private static boolean isNumeric(String value) {
        return value.chars().allMatch(Character::isDigit);
    }
}

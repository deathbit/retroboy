package com.github.deathbit.retroboy.enums;

import java.util.EnumSet;

final class StartupTaskMask {
    private final EnumSet<StartupTask> enabledTasks;

    StartupTaskMask(EnumSet<StartupTask> enabledTasks) {
        this.enabledTasks = enabledTasks;
    }

    boolean isEnabled(StartupTask startupTask) {
        return enabledTasks.contains(startupTask);
    }
}


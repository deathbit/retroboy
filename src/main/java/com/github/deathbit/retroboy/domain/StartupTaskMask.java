package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.StartupTask;

import java.util.EnumSet;

public final class StartupTaskMask {
    private final EnumSet<StartupTask> enabledTasks;

    public StartupTaskMask(EnumSet<StartupTask> enabledTasks) {
        this.enabledTasks = enabledTasks;
    }

    public boolean isEnabled(StartupTask startupTask) {
        return enabledTasks.contains(startupTask);
    }
}


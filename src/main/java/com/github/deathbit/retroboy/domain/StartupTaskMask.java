package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.BasePackTask;

import java.util.EnumSet;

public final class StartupTaskMask {
    private final EnumSet<BasePackTask> enabledTasks;

    public StartupTaskMask(EnumSet<BasePackTask> enabledTasks) {
        this.enabledTasks = enabledTasks;
    }

    public boolean isEnabled(BasePackTask basePackTask) {
        return enabledTasks.contains(basePackTask);
    }
}


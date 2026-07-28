package com.github.deathbit.retroboy.base.impl;

import com.github.deathbit.retroboy.base.BasePackHandler;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteAllTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Override
    public String name() {
        return appConfig.getDeleteAllTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getDeleteAllTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.DELETE_ALL_TASK;
    }

    @Override
    public void handle() throws Exception {
        fileComponent.deletePath(appConfig.getDeleteAllTaskConfig().getDeletePath());
    }
}

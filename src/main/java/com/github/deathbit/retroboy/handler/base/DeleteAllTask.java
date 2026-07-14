package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
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
    public void handle() throws Exception {
        fileComponent.deletePath(appConfig.getDeleteAllTaskConfig().getDeletePath());
    }
}

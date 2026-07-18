package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetUpESDEUpdateTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Override
    public String name() {
        return appConfig.getSetUpESDEUpdateTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getSetUpESDEUpdateTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.SET_UP_ESDE_UPDATE_TASK;
    }

    @Override
    public void handle() throws Exception {
        appConfig.getSetUpESDEUpdateTaskConfig().getDeletePaths().forEach(deletePath -> {
            try {
                fileComponent.deletePath(deletePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        appConfig.getSetUpESDEUpdateTaskConfig().getPathPairs().forEach(pathPair -> {
            try {
                fileComponent.copyPath(pathPair);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

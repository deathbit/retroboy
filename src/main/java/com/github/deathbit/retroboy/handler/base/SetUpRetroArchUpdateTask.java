package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetUpRetroArchUpdateTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Override
    public String name() {
        return appConfig.getSetUpRetroArchUpdateTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getSetUpRetroArchUpdateTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.SET_UP_RETROARCH_UPDATE_TASK;
    }

    @Override
    public void handle() throws Exception {
        appConfig.getSetUpRetroArchUpdateTaskConfig().getDeletePaths().forEach(deletePath -> {
            try {
                fileComponent.deletePath(deletePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        appConfig.getSetUpRetroArchUpdateTaskConfig().getPathPairs().forEach(pathPair -> {
            try {
                fileComponent.copyPath(pathPair);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

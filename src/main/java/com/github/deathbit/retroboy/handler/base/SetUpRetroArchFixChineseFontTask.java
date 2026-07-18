package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetUpRetroArchFixChineseFontTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ConfigComponent configComponent;

    @Override
    public String name() {
        return appConfig.getSetUpRetroArchFixChineseFontTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getSetUpRetroArchFixChineseFontTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.SET_UP_RETROARCH_FIX_CHINESE_FONT_TASK;
    }

    @Override
    public void handle() throws Exception {
        fileComponent.deletePath(appConfig.getSetUpRetroArchFixChineseFontTaskConfig().getDeletePath());
        fileComponent.copyPath(appConfig.getSetUpRetroArchFixChineseFontTaskConfig().getPathPair());
        configComponent.changeRetroArchConfig(appConfig.getSetUpRetroArchFixChineseFontTaskConfig().getConfigPair());
    }
}

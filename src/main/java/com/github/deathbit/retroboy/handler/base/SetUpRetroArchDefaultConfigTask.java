package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetUpRetroArchDefaultConfigTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ConfigComponent configComponent;

    @Override
    public String name() {
        return appConfig.getSetUpRetroArchDefaultConfigTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getSetUpRetroArchDefaultConfigTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.SET_UP_RETROARCH_DEFAULT_CONFIG_TASK;
    }

    @Override
    public void handle() throws Exception {
        appConfig.getSetUpRetroArchDefaultConfigTaskConfig().getConfigPairs().forEach(configPair -> {
            try {
                configComponent.changeRetroArchConfig(configPair);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

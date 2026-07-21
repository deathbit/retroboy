package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetUpRetroArchMegaBezelShaderTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ConfigComponent configComponent;

    @Override
    public String name() {
        return appConfig.getSetUpRetroArchMegaBezelShaderTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getSetUpRetroArchMegaBezelShaderTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.SET_UP_RETROARCH_MEGA_BEZEL_SHADER_TASK;
    }

    @Override
    public void handle() throws Exception {
        appConfig.getSetUpRetroArchMegaBezelShaderTaskConfig().getPathPairs().forEach(fileComponent::copyPath);
        appConfig.getSetUpRetroArchMegaBezelShaderTaskConfig().getConfigPairs().forEach(configComponent::changeRetroArchConfig);
    }
}

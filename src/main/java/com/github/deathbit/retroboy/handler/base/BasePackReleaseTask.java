package com.github.deathbit.retroboy.handler.base;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.BasePackTask;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BasePackReleaseTask implements BasePackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ReleaseComponent releaseComponent;

    @Override
    public String name() {
        return appConfig.getBasePackReleaseTaskConfig().getTaskName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getBasePackReleaseTaskConfig().isEnabled();
    }

    @Override
    public BasePackTask task() {
        return BasePackTask.BASE_PACK_RELEASE_TASK;
    }

    @Override
    public void handle() throws Exception {
        fileComponent.deletePath(appConfig.getBasePackReleaseTaskConfig().getDeletePath() + "\\"
                + appConfig.getBasePackReleaseTaskConfig().getFileName());
        releaseComponent.release(
                appConfig.getBasePackReleaseTaskConfig().getFileName(),
                appConfig.getBasePackReleaseTaskConfig().getPathPair()
        );
    }
}

package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.enums.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlatformContextInitializer {

    @Autowired
    private AppConfig appConfig;

    public PlatformContext handle(Platform platform) throws Exception {
        var platformContext = new PlatformContext();
        platformContext.setPlatform(platform);
        platformContext.setGlobalConfig(appConfig.getGlobalConfig());
        platformContext.setPlatformPackTaskConfig(appConfig.getPlatformPackTaskConfigMap().get(platform));

        return platformContext;
    }
}

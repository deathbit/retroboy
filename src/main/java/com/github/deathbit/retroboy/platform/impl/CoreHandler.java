package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreHandler {

    @Autowired
    private FileComponent fileComponent;
    public void handle(PlatformContext platformContext) throws Exception {
        fileComponent.copyPath(PathPair.builder()
                                       .sourcePath(PathUtils.platformCoreConfig(platformContext))
                                       .targetPath(PathUtils.RETROARCH_CONFIG.get(platformContext)).build());
    }
}

package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.platform.MediaHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MediaHandlerImpl implements MediaHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        fileComponent.deletePath(String.format("%s\\ES-DE\\downloaded_media\\%s",
                ruleContext.getGlobalConfig().getEsdeHomePath(), ruleContext.getPlatformName()));
        fileComponent.copyPath(PathPair.builder()
                .sourcePath(String.format("%s\\platform\\%s\\downloaded_media\\%s",
                        ruleContext.getGlobalConfig().getResourcesHomePath(),
                        ruleContext.getPlatformName(), ruleContext.getPlatformName()))
                .targetPath(String.format("%s\\ES-DE\\downloaded_media",
                        ruleContext.getGlobalConfig().getEsdeHomePath())).build());
    }
}


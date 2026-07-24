package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.platform.CoreHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreHandlerImpl implements CoreHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        fileComponent.copyPath(PathPair.builder()
                                       .sourcePath(String.format("%s\\platform\\%s\\core_config\\%s",
                                           ruleContext.getGlobalConfig().getResourcesHomePath(),
                                           ruleContext.getPlatformName(),
                                           ruleContext.getPlatformPackTaskConfig().getCore()))
                                       .targetPath(String.format("%s\\config", ruleContext.getGlobalConfig().getRetroarchHomePath())).build());
    }
}

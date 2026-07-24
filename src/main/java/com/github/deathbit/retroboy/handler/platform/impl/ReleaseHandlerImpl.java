package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.component.ReleaseComponent;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.platform.ReleaseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReleaseHandlerImpl implements ReleaseHandler {

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private ReleaseComponent releaseComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        if (ruleContext.getPlatformPackTaskConfig().isRelease()) {
            String targetPath = String.format("%s\\release\\%s.zip", ruleContext.getGlobalConfig().getResourcesHomePath(), ruleContext.getPlatform().name());
            fileComponent.deletePath(targetPath);
            var sourcePaths = new ArrayList<>(ruleContext.getPlatformPackTaskConfig().getCoreConfigs());
            sourcePaths.addAll(List.of(
                    String.format("%s\\ROMs\\%s", ruleContext.getGlobalConfig().getEsdeHomePath(), ruleContext.getPlatformName()),
                    String.format("%s\\ES-DE\\downloaded_media\\%s", ruleContext.getGlobalConfig().getEsdeHomePath(), ruleContext.getPlatformName()),
                    String.format("%s\\ES-DE\\gamelists\\%s", ruleContext.getGlobalConfig().getEsdeHomePath(), ruleContext.getPlatformName())));
            releaseComponent.releaseNew(targetPath, sourcePaths);
        }
    }
}

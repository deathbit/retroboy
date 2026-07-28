package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreHandler {

    @Autowired
    private FileComponent fileComponent;
    public void handle(RuleContext ruleContext) throws Exception {
        fileComponent.copyPath(PathPair.builder()
                                       .sourcePath(PathUtils.platformCoreConfig(ruleContext))
                                       .targetPath(PathUtils.RETROARCH_CONFIG.get(ruleContext)).build());
    }
}

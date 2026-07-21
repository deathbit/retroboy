package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.handler.component.MoveGameHandler;
import com.github.deathbit.retroboy.handler.component.RenameGameHandler;
import com.github.deathbit.retroboy.handler.component.RuleContextInitializer;
import com.github.deathbit.retroboy.handler.component.RuleEngineHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultPlatformPackHandler implements PlatformPackHandler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RuleContextInitializer ruleContextInitializer;

    @Autowired
    private RuleEngineHandler ruleEngineHandler;

    @Autowired
    private MoveGameHandler moveGameHandler;

    @Autowired
    private RenameGameHandler renameGameHandler;

    @Override
    public void handle(Platform platform) throws Exception {
        var ruleContext = ruleContextInitializer.handle(platform);
        ruleEngineHandler.handle(ruleContext);
        moveGameHandler.handle(ruleContext);
        renameGameHandler.handle(ruleContext);
    }
}

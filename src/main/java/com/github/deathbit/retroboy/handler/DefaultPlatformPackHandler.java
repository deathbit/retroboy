package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.handler.component.*;
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

    @Autowired
    private MediaHandler mediaHandler;

    @Autowired
    private GameListHandler gameListHandler;

    @Autowired
    private CoreHandler coreHandler;

    @Autowired
    private ReleaseHandler releaseHandler;

    @Override
    public void handle(Platform platform) throws Exception {
        var ruleContext = ruleContextInitializer.handle(platform);
        ruleEngineHandler.handle(ruleContext);
        moveGameHandler.handle(ruleContext);
        renameGameHandler.handle(ruleContext);
        if (ruleContext.getPlatformPackTaskConfig().isManualStep()) {
            mediaHandler.handle(ruleContext);
            gameListHandler.handle(ruleContext);
            coreHandler.handle(ruleContext);
            releaseHandler.handle(ruleContext);
        }
    }
}

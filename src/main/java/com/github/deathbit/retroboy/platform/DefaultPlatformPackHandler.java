package com.github.deathbit.retroboy.platform;

import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.platform.impl.CoreHandler;
import com.github.deathbit.retroboy.platform.impl.DebugReportHandler;
import com.github.deathbit.retroboy.platform.impl.GameListHandler;
import com.github.deathbit.retroboy.platform.impl.MediaHandler;
import com.github.deathbit.retroboy.platform.impl.MoveGameHandler;
import com.github.deathbit.retroboy.platform.impl.ReleaseHandler;
import com.github.deathbit.retroboy.platform.impl.ReleaseReportHandler;
import com.github.deathbit.retroboy.platform.impl.RenameGameHandler;
import com.github.deathbit.retroboy.platform.impl.RuleContextInitializer;
import com.github.deathbit.retroboy.platform.impl.RuleEngineHandler;
import com.github.deathbit.retroboy.platform.impl.WikiMatcherHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultPlatformPackHandler implements PlatformPackHandler {

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

    @Autowired
    private WikiMatcherHandler wikiMatcherHandler;

    @Autowired
    private DebugReportHandler debugReportHandler;

    @Autowired
    private ReleaseReportHandler releaseReportHandler;

    @Override
    public void handle(Platform platform) throws Exception {
        var ruleContext = ruleContextInitializer.handle(platform);
        ruleEngineHandler.handle(ruleContext);
        moveGameHandler.handle(ruleContext);
        renameGameHandler.handle(ruleContext);
        if (ruleContext.getPlatformPackTaskConfig().isManualStep()) {
            wikiMatcherHandler.handle(ruleContext);
            mediaHandler.handle(ruleContext);
            gameListHandler.handle(ruleContext);
            coreHandler.handle(ruleContext);
            debugReportHandler.handle(ruleContext);
            releaseReportHandler.handle(ruleContext);
            releaseHandler.handle(ruleContext);
        }
    }
}

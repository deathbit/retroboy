package com.github.deathbit.retroboy.platform;

import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.platform.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultPlatformPackHandler implements PlatformPackHandler {

    @Autowired
    private PlatformContextInitializer platformContextInitializer;

    @Autowired
    private WikiHandler wikiHandler;

    @Autowired
    private NoIntroHandler noIntroHandler;

    @Autowired
    private SSHandler ssHandler;

    @Autowired
    private FileContextHandler fileContextHandler;

    @Autowired
    private WikiGamePackageToNoIntroGamePackageMatchHandler wikiGamePackageToNoIntroGamePackageMatchHandler;

    @Autowired
    private NoIntroGamePackageToFileContextMatchHandler noIntroGamePackageToFileContextMatchHandler;

    @Autowired
    private FileContextToSSGamePackageMatchHandler fileContextToSSGamePackageMatchHandler;

    @Autowired
    private MatchPostProcessorHandler matchPostProcessorHandler;

    @Autowired
    private MediaDownloaderHandler mediaDownloaderHandler;

    @Autowired
    private MoveHandler moveHandler;

    @Autowired
    private RenameHandler renameHandler;

    @Autowired
    private GameListHandler gameListHandler;

    @Autowired
    private MediaHandler mediaHandler;

    @Autowired
    private MappedAreaOutputHandler mappedAreaOutputHandler;

    @Autowired
    private CoreHandler coreHandler;

    @Autowired
    private DebugReportHandler debugReportHandler;

    @Autowired
    private ReleaseReportHandler releaseReportHandler;

    @Autowired
    private ReleaseHandler releaseHandler;

    @Override
    public void handle(Platform platform) throws Exception {
        var platformContext = platformContextInitializer.handle(platform);
        wikiHandler.handle(platformContext);
        noIntroHandler.handle(platformContext);
        ssHandler.handle(platformContext);
        fileContextHandler.handle(platformContext);
        wikiGamePackageToNoIntroGamePackageMatchHandler.handle(platformContext);
        noIntroGamePackageToFileContextMatchHandler.handle(platformContext);
        fileContextToSSGamePackageMatchHandler.handle(platformContext);
        matchPostProcessorHandler.handle(platformContext);
        mediaDownloaderHandler.handle(platformContext);
        moveHandler.handle(platformContext);
        renameHandler.handle(platformContext);
        gameListHandler.handle(platformContext);
        mediaHandler.handle(platformContext);
        mappedAreaOutputHandler.handle(platformContext);
        coreHandler.handle(platformContext);
        debugReportHandler.handle(platformContext);

        System.out.println();






        releaseReportHandler.handle(platformContext);
        releaseHandler.handle(platformContext);
        System.out.println();
    }
}

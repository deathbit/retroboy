package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupRunner implements ApplicationRunner {

    private final AppConfig appConfig;
    private final CleanUpComponent cleanUpComponent;

    public StartupRunner(AppConfig appConfig, CleanUpComponent cleanUpComponent) {
        this.appConfig = appConfig;
        this.cleanUpComponent = cleanUpComponent;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        cleanUpComponent.batchDeleteDir(appConfig.getCleanup().getCleanupDirs());
        cleanUpComponent.batchDdeleteFile(appConfig.getCleanup().getCleanupFiles());
    }
}

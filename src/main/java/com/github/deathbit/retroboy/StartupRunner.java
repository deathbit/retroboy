package com.github.deathbit.retroboy;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.handler.BasePackHandler;
import com.github.deathbit.retroboy.handler.PlatformPackHandler;
import com.github.deathbit.retroboy.utils.CommonUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private List<BasePackHandler> basePackHandlers;

    @Autowired
    private PlatformPackHandler platformPackHandler;

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        buildingBasePack();
        buildingPlatformPack();
    }

    private void buildingBasePack() throws Exception {
        CommonUtils.printAsciiArt();
        if (appConfig.getGlobalConfig().isEnableBasePackHandler()) {
            basePackHandlers.stream().sorted(Comparator.comparing(BasePackHandler::task)).forEach(handler -> {
                try {
                    if (handler.enabled()) {
                        CommonUtils.printTask(handler.name());
                        handler.handle();
                        CommonUtils.printTaskDone(handler.name());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void buildingPlatformPack() throws Exception {
        if (appConfig.getGlobalConfig().isEnablePlatformPackHandler()) {
            appConfig.getPlatformPackTaskConfigMap().forEach((platform, platformPackTaskConfig) -> {
                try {
                    if (platformPackTaskConfig.isEnabled()) {
                        CommonUtils.printTask("构建%s平台包".formatted(platform.name()));
                        platformPackHandler.handle(platform);
                        CommonUtils.printTaskDone("构建%s平台包".formatted(platform.name()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        System.exit(0);
    }
}

package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.domain.HandlerMediaTypes;
import com.github.deathbit.retroboy.domain.MediaType;
import com.github.deathbit.retroboy.handler.component.CheckMissingMediaFilesHandlerComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class CheckMissingMediaFilesHandlerComponentImpl implements CheckMissingMediaFilesHandlerComponent {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        var downloadedMediaDirBase = appConfig.getGlobalConfig().getDownloadedMediaDirBase();
        if (downloadedMediaDirBase == null || downloadedMediaDirBase.isBlank()) {
            throw new IllegalStateException("app.config.globalConfig.downloadedMediaDirBase must be configured");
        }

        var platformMediaDirName = Paths.get(ruleContext.getRuleConfig().getTargetDirBase()).getFileName().toString();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var area = areaConfig.getArea();
            var areaTargetDir = Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), area.name());
            try (var files = Files.list(areaTargetDir)) {
                var gameNames = files
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .sorted()
                        .map(this::withoutExtension)
                        .toList();
                addMissingMediaReportLines(ruleContext, downloadedMediaDirBase, platformMediaDirName, area, gameNames);
            }
        }
    }

    private void addMissingMediaReportLines(RuleContext ruleContext,
                                            String downloadedMediaDirBase,
                                            String platformMediaDirName,
                                            Area area,
                                            List<String> gameNames) {
        var missingMediaReportMap = ruleContext.getAreaMissingMediaReportMap().get(area);
        for (var gameName : gameNames) {
            for (MediaType mediaType : HandlerMediaTypes.MEDIA_TYPES) {
                var mediaFile = Paths.get(
                        downloadedMediaDirBase,
                        platformMediaDirName,
                        mediaType.name(),
                        area.name(),
                        gameName + mediaType.reportExtension());
                if (!mediaFileExists(mediaFile, mediaType.fallbackExtension())) {
                    missingMediaReportMap.get(mediaType.name()).add(mediaFile.toString());
                }
            }
        }
    }

    private boolean mediaFileExists(Path mediaFile, String fallbackExtension) {
        if (Files.isRegularFile(mediaFile)) {
            return true;
        }
        if (fallbackExtension == null) {
            return false;
        }
        return Files.isRegularFile(withExtension(mediaFile, fallbackExtension));
    }

    private Path withExtension(Path path, String extension) {
        var fileName = path.getFileName().toString();
        return path.resolveSibling(withoutExtension(fileName) + extension);
    }

    private String withoutExtension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }
}


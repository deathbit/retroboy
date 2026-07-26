package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.domain.WikiGameEntry;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.handler.platform.MediaHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MediaHandlerImpl implements MediaHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        fileComponent.deletePath(String.format("%s\\ES-DE\\downloaded_media\\%s",
                ruleContext.getGlobalConfig().getEsdeHomePath(), ruleContext.getPlatformName()));
        fileComponent.copyPath(PathPair.builder()
                .sourcePath(String.format("%s\\platform\\%s\\downloaded_media\\%s",
                        ruleContext.getGlobalConfig().getResourcesHomePath(),
                        ruleContext.getPlatformName(), ruleContext.getPlatformName()))
                .targetPath(String.format("%s\\ES-DE\\downloaded_media",
                        ruleContext.getGlobalConfig().getEsdeHomePath())).build());
        checkMedia(ruleContext);
    }

    private void checkMedia(RuleContext ruleContext) {
        if (ruleContext.getAreaWikiEntryMap() == null || ruleContext.getAreaWikiEntryMap().isEmpty()) {
            throw new IllegalStateException("areaWikiEntryMap is empty, run WikiMatcherHandler before MediaHandler");
        }

        var mediaRootPath = Path.of(ruleContext.getGlobalConfig().getEsdeHomePath(),
                "ES-DE",
                "downloaded_media",
                ruleContext.getPlatformName());
        ruleContext.getAreaWikiEntryMap().forEach((area, wikiEntryMap) ->
                wikiEntryMap.values().forEach(wikiGameEntry ->
                        wikiGameEntry.setMissingMediaBitmap(buildMissingMediaBitmap(ruleContext, mediaRootPath, area, wikiGameEntry))));
    }

    private int buildMissingMediaBitmap(RuleContext ruleContext, Path mediaRootPath, Area area, WikiGameEntry wikiGameEntry) {
        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
        if (areaRenameResult == null || areaRenameResult.getFinalName() == null || areaRenameResult.getFinalName().isBlank()) {
            return MediaAssetType.allMissingBitmap();
        }

        var missingMediaBitmap = 0;
        var mediaAreaDirectoryName = ruleContext.getPlatform().name() + "-" + area.name();
        for (var mediaAssetType : MediaAssetType.values()) {
            if (!existsMediaFile(mediaRootPath, mediaAssetType, mediaAreaDirectoryName, areaRenameResult.getFinalName())) {
                missingMediaBitmap |= mediaAssetType.getBitMask();
            }
        }
        return missingMediaBitmap;
    }

    private boolean existsMediaFile(Path mediaRootPath,
                                   MediaAssetType mediaAssetType,
                                   String mediaAreaDirectoryName,
                                   String finalName) {
        var primaryPath = buildMediaPath(mediaRootPath, mediaAssetType, mediaAreaDirectoryName, finalName,
                mediaAssetType.getPrimaryExtension());
        if (Files.isRegularFile(primaryPath)) {
            return true;
        }

        var fallbackExtension = mediaAssetType.getFallbackExtension();
        return fallbackExtension != null
                && Files.isRegularFile(buildMediaPath(mediaRootPath, mediaAssetType, mediaAreaDirectoryName, finalName,
                fallbackExtension));
    }

    private Path buildMediaPath(Path mediaRootPath,
                                MediaAssetType mediaAssetType,
                                String mediaAreaDirectoryName,
                                String finalName,
                                String extension) {
        return mediaRootPath.resolve(mediaAssetType.getDirectoryName())
                .resolve(mediaAreaDirectoryName)
                .resolve(finalName + "." + extension);
    }
}

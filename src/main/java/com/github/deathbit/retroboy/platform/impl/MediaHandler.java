package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.domain.WikiGameEntry;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MediaHandler {

    @Autowired
    private FileComponent fileComponent;
    public void handle(RuleContext ruleContext) throws Exception {
        fileComponent.deletePath(PathUtils.ESDE_PLATFORM_MEDIA.get(ruleContext));
        fileComponent.copyPath(PathPair.builder()
                .sourcePath(PathUtils.PLATFORM_DOWNLOADED_MEDIA.get(ruleContext))
                .targetPath(PathUtils.ESDE_MEDIA_ROOT.get(ruleContext)).build());
        checkMedia(ruleContext);
    }

    private void checkMedia(RuleContext ruleContext) {
        if (ruleContext.getAreaWikiEntryMap() == null || ruleContext.getAreaWikiEntryMap().isEmpty()) {
            throw new IllegalStateException("areaWikiEntryMap is empty, run WikiMatcherHandler before MediaHandler");
        }

        ruleContext.getAreaWikiEntryMap().forEach((area, wikiEntryMap) ->
                wikiEntryMap.values().forEach(wikiGameEntry ->
                        wikiGameEntry.setMissingMediaBitmap(buildMissingMediaBitmap(ruleContext, area, wikiGameEntry))));
    }

    private int buildMissingMediaBitmap(RuleContext ruleContext, Area area, WikiGameEntry wikiGameEntry) {
        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
        if (areaRenameResult == null || areaRenameResult.getFinalName() == null || areaRenameResult.getFinalName().isBlank()) {
            return MediaAssetType.allMissingBitmap();
        }

        var missingMediaBitmap = 0;
        var mediaAreaDirectoryName = ruleContext.getPlatform().name() + "-" + area.name();
        for (var mediaAssetType : MediaAssetType.values()) {
            if (!existsMediaFile(ruleContext, mediaAssetType, mediaAreaDirectoryName, areaRenameResult.getFinalName())) {
                missingMediaBitmap |= mediaAssetType.getBitMask();
            }
        }
        return missingMediaBitmap;
    }

    private boolean existsMediaFile(RuleContext ruleContext,
                                   MediaAssetType mediaAssetType,
                                   String mediaAreaDirectoryName,
                                   String finalName) {
        var primaryPath = buildMediaPath(ruleContext, mediaAssetType, mediaAreaDirectoryName, finalName,
                mediaAssetType.getPrimaryExtension());
        if (Files.isRegularFile(primaryPath)) {
            return true;
        }

        var fallbackExtension = mediaAssetType.getFallbackExtension();
        return fallbackExtension != null
                && Files.isRegularFile(buildMediaPath(ruleContext, mediaAssetType, mediaAreaDirectoryName, finalName,
                fallbackExtension));
    }

    private Path buildMediaPath(RuleContext ruleContext,
                                MediaAssetType mediaAssetType,
                                String mediaAreaDirectoryName,
                                String finalName,
                                String extension) {
        return PathUtils.esdeMedia(ruleContext, mediaAssetType, mediaAreaDirectoryName, finalName, extension);
    }
}

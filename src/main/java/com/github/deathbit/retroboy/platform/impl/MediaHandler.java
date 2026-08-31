package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.MediaCompletionRate;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.util.MediaBitmapUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MediaHandler {

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) throws Exception {
        validateRenameResults(platformContext);
        fileComponent.deletePath(PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext));
        fileComponent.copyPath(PathPair.builder()
                                       .sourcePath(PathUtils.PLATFORM_DOWNLOADED_MEDIA.get(platformContext))
                                       .targetPath(PathUtils.ESDE_MEDIA_ROOT.get(platformContext)).build());
        platformContext.setMediaCompletionRateMap(populateMediaBitmapsAndBuildCompletionRates(platformContext));
    }

    private void validateRenameResults(PlatformContext platformContext) {
        if (platformContext.getRenameResultByArea() == null || platformContext.getRenameResultByArea().isEmpty()) {
            throw new IllegalStateException("renameResultByArea is empty, run RenameHandler before MediaHandler");
        }
        if (platformContext.getFinalGameMapByArea() == null || platformContext.getFinalGameMapByArea().isEmpty()) {
            throw new IllegalStateException("finalGameMapByArea is empty, run RenameHandler before MediaHandler");
        }
    }

    private Map<String, Map<MediaAssetType, MediaCompletionRate>> populateMediaBitmapsAndBuildCompletionRates(
            PlatformContext platformContext) {
        var mediaCompletionRateMap = new LinkedHashMap<String, Map<MediaAssetType, MediaCompletionRate>>();
        platformContext.getFinalGameMapByArea().forEach((area, finalGameMap) -> {
            var mediaAssetRateMap = new EnumMap<MediaAssetType, MediaCompletionRate>(MediaAssetType.class);
            var completedCountByType = new EnumMap<MediaAssetType, Integer>(MediaAssetType.class);
            for (var mediaAssetType : MediaAssetType.values()) {
                completedCountByType.put(mediaAssetType, 0);
            }

            var mediaAreaDirectoryName = PathUtils.esdeAreaDirectoryName(platformContext, area);
            for (var finalGame : finalGameMap.values()) {
                var mediaBitmap = 0;
                for (var mediaAssetType : MediaAssetType.values()) {
                    if (existsMediaFile(platformContext, mediaAssetType, mediaAreaDirectoryName, finalGame.getFinalRomName())) {
                        mediaBitmap = MediaBitmapUtils.withMedia(mediaBitmap, mediaAssetType);
                        completedCountByType.compute(mediaAssetType, (ignored, count) -> count + 1);
                    }
                }
                finalGame.setMediaBitMap(mediaBitmap);
            }

            var totalCount = finalGameMap.size();
            for (var mediaAssetType : MediaAssetType.values()) {
                mediaAssetRateMap.put(mediaAssetType,
                        MediaCompletionRate.of(totalCount, completedCountByType.get(mediaAssetType)));
            }
            mediaCompletionRateMap.put(area, mediaAssetRateMap);
        });
        return mediaCompletionRateMap;
    }

    private boolean existsMediaFile(PlatformContext platformContext,
                                   MediaAssetType mediaAssetType,
                                   String mediaAreaDirectoryName,
                                   String finalName) {
        if (finalName == null || finalName.isBlank()) {
            return false;
        }

        var primaryPath = buildMediaPath(platformContext, mediaAssetType, mediaAreaDirectoryName, finalName,
                mediaAssetType.getPrimaryExtension());
        if (Files.isRegularFile(primaryPath)) {
            return true;
        }

        var fallbackExtension = mediaAssetType.getFallbackExtension();
        return fallbackExtension != null
                && Files.isRegularFile(buildMediaPath(platformContext, mediaAssetType, mediaAreaDirectoryName, finalName,
                fallbackExtension));
    }

    private Path buildMediaPath(PlatformContext platformContext,
                                MediaAssetType mediaAssetType,
                                String mediaAreaDirectoryName,
                                String finalName,
                                String extension) {
        return PathUtils.esdeMedia(platformContext, mediaAssetType, mediaAreaDirectoryName, finalName, extension);
    }
}

//package com.github.deathbit.retroboy.platform.impl;
//
//import com.github.deathbit.retroboy.component.FileComponent;
//import com.github.deathbit.retroboy.domain.MediaCompletionRate;
//import com.github.deathbit.retroboy.domain.PathPair;
//import com.github.deathbit.retroboy.domain.PlatformContext;
//import com.github.deathbit.retroboy.domain.WikiGameEntry;
//import com.github.deathbit.retroboy.enums.Area;
//import com.github.deathbit.retroboy.enums.MediaAssetType;
//import com.github.deathbit.retroboy.util.PathUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.EnumMap;
//import java.util.Map;
//
//@Component
//public class MediaHandler {
//
//    @Autowired
//    private FileComponent fileComponent;
//    public void handle(PlatformContext platformContext) throws Exception {
//        fileComponent.deletePath(PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext));
//        fileComponent.copyPath(PathPair.builder()
//                                       .sourcePath(PathUtils.PLATFORM_DOWNLOADED_MEDIA.get(platformContext))
//                                       .targetPath(PathUtils.ESDE_MEDIA_ROOT.get(platformContext)).build());
//        checkMedia(platformContext);
//    }
//
//    private void checkMedia(PlatformContext platformContext) {
//        if (platformContext.getAreaWikiEntryMap() == null || platformContext.getAreaWikiEntryMap().isEmpty()) {
//            throw new IllegalStateException("areaWikiEntryMap is empty, run WikiMatcherHandler before MediaHandler");
//        }
//
//        platformContext.getAreaWikiEntryMap().forEach((area, wikiEntryMap) ->
//                wikiEntryMap.values().forEach(wikiGameEntry ->
//                        wikiGameEntry.setMissingMediaBitmap(buildMissingMediaBitmap(platformContext, area, wikiGameEntry))));
//        platformContext.setMediaCompletionRateMap(buildMediaCompletionRateMap(platformContext));
//    }
//
//    private Map<Area, Map<MediaAssetType, MediaCompletionRate>> buildMediaCompletionRateMap(PlatformContext platformContext) {
//        var mediaCompletionRateMap = new EnumMap<Area, Map<MediaAssetType, MediaCompletionRate>>(Area.class);
//        platformContext.getAreaWikiEntryMap().forEach((area, wikiEntryMap) -> {
//            var mediaAssetRateMap = new EnumMap<MediaAssetType, MediaCompletionRate>(MediaAssetType.class);
//            var totalCount = wikiEntryMap.size();
//            for (var mediaAssetType : MediaAssetType.values()) {
//                var completedCount = (int) wikiEntryMap.values()
//                        .stream()
//                        .filter(wikiGameEntry -> !wikiGameEntry.isMediaMissing(mediaAssetType))
//                        .count();
//                mediaAssetRateMap.put(mediaAssetType, MediaCompletionRate.of(totalCount, completedCount));
//            }
//            mediaCompletionRateMap.put(area, mediaAssetRateMap);
//        });
//        return mediaCompletionRateMap;
//    }
//
//    private int buildMissingMediaBitmap(PlatformContext platformContext, Area area, WikiGameEntry wikiGameEntry) {
//        var areaRenameResult = wikiGameEntry.getAreaRenameResult();
//        if (areaRenameResult == null || areaRenameResult.getFinalName() == null || areaRenameResult.getFinalName().isBlank()) {
//            return MediaAssetType.allMissingBitmap();
//        }
//
//        var missingMediaBitmap = 0;
//        var mediaAreaDirectoryName = PathUtils.esdeAreaDirectoryName(platformContext, area);
//        for (var mediaAssetType : MediaAssetType.values()) {
//            if (!existsMediaFile(platformContext, mediaAssetType, mediaAreaDirectoryName, areaRenameResult.getFinalName())) {
//                missingMediaBitmap |= mediaAssetType.getBitMask();
//            }
//        }
//        return missingMediaBitmap;
//    }
//
//    private boolean existsMediaFile(PlatformContext platformContext,
//                                   MediaAssetType mediaAssetType,
//                                   String mediaAreaDirectoryName,
//                                   String finalName) {
//        var primaryPath = buildMediaPath(platformContext, mediaAssetType, mediaAreaDirectoryName, finalName,
//                mediaAssetType.getPrimaryExtension());
//        if (Files.isRegularFile(primaryPath)) {
//            return true;
//        }
//
//        var fallbackExtension = mediaAssetType.getFallbackExtension();
//        return fallbackExtension != null
//                && Files.isRegularFile(buildMediaPath(platformContext, mediaAssetType, mediaAreaDirectoryName, finalName,
//                fallbackExtension));
//    }
//
//    private Path buildMediaPath(PlatformContext platformContext,
//                                MediaAssetType mediaAssetType,
//                                String mediaAreaDirectoryName,
//                                String finalName,
//                                String extension) {
//        return PathUtils.esdeMedia(platformContext, mediaAssetType, mediaAreaDirectoryName, finalName, extension);
//    }
//}

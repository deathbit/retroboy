package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.FinalGame;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.MediaCompletionRate;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.util.MediaBitmapUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/*
 * 媒体匹配策略：
 *
 * 通用地区规则：
 *   JPN：JPN > WOR > SS > USA > EUR > 任一其它地区
 *   USA：USA > WOR > EUR > SS > 除 JPN 任一其它地区 > JPN
 *   其它：本身地区 > EUR > WOR > USA > SS > 除 JPN 任一其它地区 > JPN
 *
 * fanart、video：
 *   不使用地区规则，只要存在该类型媒体就使用。
 *
 * support-2D：
 *   JPN 只匹配 JPN 地区媒体。
 *
 * marquees：
 *   先匹配 wheel，未命中时再匹配 wheel-hd。
 */
@Component
public class MediaHandler {

    private static final String JAPAN_REGION = "JPN";
    private static final String USA_REGION = "USA";
    private static final String EUROPE_REGION = "EUR";
    private static final String WORLD_REGION = "WOR";
    private static final String SPECIAL_REGION = "SS";
    private static final Map<MediaAssetType, SourceMediaSpec> SOURCE_MEDIA_SPECS = buildSourceMediaSpecs();

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) throws Exception {
        var finalGameMapByArea = requireFinalGameMapByArea(platformContext);
        resetMediaBitmaps(finalGameMapByArea);
        fileComponent.deletePath(PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext));

        var copyItems = buildCopyItems(platformContext, finalGameMapByArea);
        ProgressBar pb = new ProgressBar("复制媒体");
        pb.startTask(copyItems.size());
        for (int i = 0; i < copyItems.size(); i++) {
            var copyItem = copyItems.get(i);
            if (Files.exists(copyItem.sourcePath())) {
                Files.createDirectories(copyItem.targetPath().getParent());
                Files.copy(copyItem.sourcePath(), copyItem.targetPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();

        platformContext.setMediaCompletionRateMap(populateMediaBitmapsAndBuildCompletionRates(platformContext, finalGameMapByArea));
    }

    private static Map<MediaAssetType, SourceMediaSpec> buildSourceMediaSpecs() {
        var specs = new LinkedHashMap<MediaAssetType, SourceMediaSpec>();
        specs.put(MediaAssetType.THREE_D_BOX, new SourceMediaSpec("box-3D", true));
        specs.put(MediaAssetType.BACK_COVER, new SourceMediaSpec("box-2D-back", true));
        specs.put(MediaAssetType.COVER, new SourceMediaSpec("box-2D", true));
        specs.put(MediaAssetType.FANART, new SourceMediaSpec("fanart", false));
        specs.put(MediaAssetType.MANUAL, new SourceMediaSpec("manuel", true));
        specs.put(MediaAssetType.MARQUEE, new SourceMediaSpec(List.of("wheel", "wheel-hd"), true));
        specs.put(MediaAssetType.PHYSICAL_MEDIA, new SourceMediaSpec("support-2D", true));
        specs.put(MediaAssetType.SCREENSHOT, new SourceMediaSpec("ss", true));
        specs.put(MediaAssetType.TITLE_SCREEN, new SourceMediaSpec("sstitle", true));
        specs.put(MediaAssetType.VIDEO, new SourceMediaSpec("video", false));
        return specs;
    }

    private Map<String, Map<String, FinalGame>> requireFinalGameMapByArea(PlatformContext platformContext) {
        var finalGameMapByArea = platformContext.getFinalGameMapByArea();
        if (finalGameMapByArea == null || finalGameMapByArea.isEmpty()) {
            throw new IllegalStateException("finalGameMapByArea is empty, run RenameHandler before MediaHandler");
        }
        return finalGameMapByArea;
    }

    private void resetMediaBitmaps(Map<String, Map<String, FinalGame>> finalGameMapByArea) {
        finalGameMapByArea.values().forEach(finalGames ->
                finalGames.values().forEach(finalGame -> finalGame.setMediaBitMap(0)));
    }

    private List<CopyItem> buildCopyItems(PlatformContext platformContext,
                                          Map<String, Map<String, FinalGame>> finalGameMapByArea) throws IOException {
        if (platformContext.getMatchResults() == null || platformContext.getMatchResults().isEmpty()) {
            throw new IllegalStateException("matchResults is empty, run FileContextToSSGamePackageMatchHandler before MediaHandler");
        }

        var mediaCatalog = buildMediaCatalog(platformContext);
        var copyItems = new ArrayList<CopyItem>();
        for (var matchResult : platformContext.getMatchResults()) {
            collectCopyItems(platformContext, matchResult, finalGameMapByArea, mediaCatalog, copyItems);
        }
        return copyItems;
    }

    private void collectCopyItems(PlatformContext platformContext,
                                  MatchResult matchResult,
                                  Map<String, Map<String, FinalGame>> finalGameMapByArea,
                                  MediaCatalog mediaCatalog,
                                  List<CopyItem> copyItems) {
        if (matchResult.getFileContextByArea() == null || matchResult.getFileContextByArea().isEmpty()) {
            return;
        }
        if (matchResult.getRenameResultByArea() == null || matchResult.getRenameResultByArea().isEmpty()) {
            throw new IllegalStateException("renameResultByArea is empty, run RenameHandler before MediaHandler");
        }

        for (var entry : matchResult.getFileContextByArea().entrySet()) {
            var area = entry.getKey();
            var fileContext = entry.getValue();
            var renameResult = matchResult.getRenameResultByArea().get(area);
            if (renameResult == null) {
                throw new IllegalStateException("Rename result not found for area: " + area);
            }

            var finalRomName = renameResult.get(fileContext.getFileName());
            if (finalRomName == null || finalRomName.isBlank()) {
                throw new IllegalStateException("Rename result not found for area=%s, file=%s"
                        .formatted(area, fileContext.getFileName()));
            }

            validateFinalGame(finalGameMapByArea, area, finalRomName);
            var ssGamePackage = requireSSGamePackage(matchResult, area);
            collectCopyItems(platformContext, area, finalRomName, ssGamePackage, mediaCatalog, copyItems);
        }
    }

    private void validateFinalGame(Map<String, Map<String, FinalGame>> finalGameMapByArea,
                                   String area,
                                   String finalRomName) {
        var finalGames = finalGameMapByArea.get(area);
        if (finalGames == null || finalGames.isEmpty()) {
            throw new IllegalStateException("Final games not found for area: " + area);
        }
        var finalGame = finalGames.get(finalRomName);
        if (finalGame == null) {
            throw new IllegalStateException("Final game not found for area=%s, finalRomName=%s"
                    .formatted(area, finalRomName));
        }
    }

    private SSGamePackage requireSSGamePackage(MatchResult matchResult, String area) {
        var ssGamePackageByArea = matchResult.getSsGamePackageByArea();
        if (ssGamePackageByArea == null || ssGamePackageByArea.isEmpty()) {
            throw new IllegalStateException("SSGamePackage not found for area: " + area);
        }

        var ssGamePackage = ssGamePackageByArea.get(area);
        if (ssGamePackage == null) {
            ssGamePackage = ssGamePackageByArea.values().stream()
                    .filter(candidate -> candidate != null
                            && candidate.getSsGameByArea() != null
                            && candidate.getSsGameByArea().containsKey(area))
                    .findFirst()
                    .orElse(null);
        }
        if (ssGamePackage == null) {
            throw new IllegalStateException("SSGamePackage not found for area: " + area);
        }
        if (ssGamePackage.getId() == null || ssGamePackage.getId().isBlank()) {
            throw new IllegalStateException("SSGamePackage id is empty for area: " + area);
        }
        return ssGamePackage;
    }

    private void collectCopyItems(PlatformContext platformContext,
                                  String area,
                                  String finalRomName,
                                  SSGamePackage ssGamePackage,
                                  MediaCatalog mediaCatalog,
                                  List<CopyItem> copyItems) {
        SOURCE_MEDIA_SPECS.forEach((mediaAssetType, spec) -> {
            var candidates = mediaCatalog.mediaByPackageIdAndType()
                    .getOrDefault(ssGamePackage.getId(), Map.of())
                    .getOrDefault(mediaAssetType, List.of());
            var sourceMedia = selectSourceMedia(
                    platformContext,
                    candidates,
                    area,
                    mediaAssetType,
                    spec,
                    finalRomName,
                    mediaCatalog.brokenMediaTargets());
            if (sourceMedia == null) {
                return;
            }

            var targetPath = mediaTargetPath(platformContext, area, finalRomName, mediaAssetType, sourceMedia.extension());
            copyItems.add(new CopyItem(sourceMedia.path(), targetPath));
        });
    }

    private MediaCatalog buildMediaCatalog(PlatformContext platformContext) throws IOException {
        var brokenMediaTargets = buildBrokenMediaTargets(platformContext);
        var brokenSourceMediaPaths = buildBrokenSourceMediaPaths(platformContext);
        var mediaByPackageIdAndType = new LinkedHashMap<String, Map<MediaAssetType, List<SourceMedia>>>();
        scanSourceMediaRoot(platformContext, "ss", 0, true, brokenSourceMediaPaths, mediaByPackageIdAndType);
        scanSourceMediaRoot(platformContext, "ss_manual", 1, false, brokenSourceMediaPaths, mediaByPackageIdAndType);

        for (var mediaByType : mediaByPackageIdAndType.values()) {
            for (var entry : mediaByType.entrySet()) {
                entry.getValue().sort(sourceMediaComparator(entry.getKey()));
            }
        }
        return new MediaCatalog(mediaByPackageIdAndType, brokenMediaTargets);
    }

    private Set<Path> buildBrokenMediaTargets(PlatformContext platformContext) {
        var brokenMediaList = platformContext.getPlatformPackTaskConfig().getBrokenMediaList();
        if (brokenMediaList == null || brokenMediaList.isEmpty()) {
            return Set.of();
        }

        var brokenMediaTargets = new HashSet<Path>();
        for (var brokenMedia : brokenMediaList) {
            var path = brokenMedia.isAbsolute()
                    ? brokenMedia
                    : PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext).resolve(brokenMedia);
            brokenMediaTargets.add(normalizedPath(path));
        }
        return brokenMediaTargets;
    }

    private Set<Path> buildBrokenSourceMediaPaths(PlatformContext platformContext) {
        var brokenMediaList = platformContext.getPlatformPackTaskConfig().getBrokenMediaList();
        if (brokenMediaList == null || brokenMediaList.isEmpty()) {
            return Set.of();
        }

        var sourceMediaRoot = PathUtils.PLATFORM_RESOURCE_ROOT.get(platformContext).resolve("ss");
        var brokenSourceMediaPaths = new HashSet<Path>();
        for (var brokenMedia : brokenMediaList) {
            brokenSourceMediaPaths.add(normalizedPath(brokenMedia.isAbsolute()
                    ? brokenMedia
                    : sourceMediaRoot.resolve(brokenMedia)));
        }
        return brokenSourceMediaPaths;
    }

    private void scanSourceMediaRoot(PlatformContext platformContext,
                                     String sourceRootName,
                                     int sourceRootRank,
                                     boolean primarySourceRoot,
                                     Set<Path> brokenSourceMediaPaths,
                                     Map<String, Map<MediaAssetType, List<SourceMedia>>> mediaByPackageIdAndType)
            throws IOException {
        var sourceRoot = PathUtils.PLATFORM_RESOURCE_ROOT.get(platformContext).resolve(sourceRootName);
        if (Files.notExists(sourceRoot)) {
            return;
        }

        try (var packageDirectories = Files.list(sourceRoot)) {
            for (var packageDirectory : packageDirectories.filter(Files::isDirectory).toList()) {
                scanPackageMediaDirectory(
                        packageDirectory,
                        sourceRootRank,
                        primarySourceRoot,
                        brokenSourceMediaPaths,
                        mediaByPackageIdAndType);
            }
        }
    }

    private void scanPackageMediaDirectory(Path packageDirectory,
                                           int sourceRootRank,
                                           boolean primarySourceRoot,
                                           Set<Path> brokenSourceMediaPaths,
                                           Map<String, Map<MediaAssetType, List<SourceMedia>>> mediaByPackageIdAndType)
            throws IOException {
        var packageId = packageDirectory.getFileName().toString();
        try (var files = Files.list(packageDirectory)) {
            for (var path : files.filter(Files::isRegularFile).toList()) {
                if (brokenSourceMediaPaths.contains(normalizedPath(path))) {
                    continue;
                }

                var sourceMedia = parseSourceMedia(packageId, path, sourceRootRank, primarySourceRoot);
                if (sourceMedia == null) {
                    continue;
                }

                mediaByPackageIdAndType
                        .computeIfAbsent(packageId, ignored -> new EnumMap<>(MediaAssetType.class))
                        .computeIfAbsent(sourceMedia.mediaAssetType(), ignored -> new ArrayList<>())
                        .add(sourceMedia);
            }
        }
    }

    private SourceMedia parseSourceMedia(String packageId, Path path, int sourceRootRank, boolean primarySourceRoot) {
        var fileName = path.getFileName().toString();
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return null;
        }

        var extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        var nameWithoutExtension = fileName.substring(0, dotIndex);
        var prefix = packageId + "_";
        if (!nameWithoutExtension.toUpperCase(Locale.ROOT).startsWith(prefix.toUpperCase(Locale.ROOT))) {
            return null;
        }

        var mediaTypeAndRegion = nameWithoutExtension.substring(prefix.length());
        var regionSeparatorIndex = mediaTypeAndRegion.lastIndexOf('_');
        if (regionSeparatorIndex == -1 || regionSeparatorIndex == mediaTypeAndRegion.length() - 1) {
            return null;
        }

        var ssMediaType = mediaTypeAndRegion.substring(0, regionSeparatorIndex);
        var mediaAssetType = mediaAssetType(ssMediaType);
        if (mediaAssetType == null || !supportsExtension(extension, mediaAssetType)) {
            return null;
        }

        return new SourceMedia(
                path,
                extension,
                mediaTypeAndRegion.substring(regionSeparatorIndex + 1).toUpperCase(Locale.ROOT),
                mediaAssetType,
                sourceMediaTypeRank(mediaAssetType, ssMediaType),
                sourceRootRank,
                primarySourceRoot);
    }

    private MediaAssetType mediaAssetType(String ssMediaType) {
        for (var entry : SOURCE_MEDIA_SPECS.entrySet()) {
            for (var candidate : entry.getValue().ssMediaTypes()) {
                if (candidate.equalsIgnoreCase(ssMediaType)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private int sourceMediaTypeRank(MediaAssetType mediaAssetType, String ssMediaType) {
        var ssMediaTypes = SOURCE_MEDIA_SPECS.get(mediaAssetType).ssMediaTypes();
        for (int i = 0; i < ssMediaTypes.size(); i++) {
            if (ssMediaTypes.get(i).equalsIgnoreCase(ssMediaType)) {
                return i;
            }
        }
        return ssMediaTypes.size();
    }

    private boolean supportsExtension(String extension, MediaAssetType mediaAssetType) {
        return mediaAssetType.getPrimaryExtension().equalsIgnoreCase(extension)
                || mediaAssetType.getFallbackExtension() != null
                && mediaAssetType.getFallbackExtension().equalsIgnoreCase(extension);
    }

    private Comparator<SourceMedia> sourceMediaComparator(MediaAssetType mediaAssetType) {
        return Comparator.comparingInt(SourceMedia::sourceMediaTypeRank)
                .thenComparingInt(SourceMedia::sourceRootRank)
                .thenComparingInt(sourceMedia -> extensionRank(sourceMedia, mediaAssetType))
                .thenComparing(sourceMedia -> sourceMedia.path().toString());
    }

    private int extensionRank(SourceMedia sourceMedia, MediaAssetType mediaAssetType) {
        return mediaAssetType.getPrimaryExtension().equalsIgnoreCase(sourceMedia.extension()) ? 0 : 1;
    }

    private SourceMedia selectSourceMedia(PlatformContext platformContext,
                                          List<SourceMedia> candidates,
                                          String area,
                                          MediaAssetType mediaAssetType,
                                          SourceMediaSpec spec,
                                          String finalRomName,
                                          Set<Path> brokenMediaTargets) {
        if (mediaAssetType == MediaAssetType.PHYSICAL_MEDIA && JAPAN_REGION.equals(area)) {
            var japanCandidates = candidates.stream()
                    .filter(sourceMedia -> JAPAN_REGION.equals(sourceMedia.region()))
                    .toList();
            var sourceMedia = japanCandidates.isEmpty() ? null : japanCandidates.get(0);
            if (sourceMedia == null || !sourceMedia.primarySourceRoot()) {
                return sourceMedia;
            }

            var targetPath = mediaTargetPath(platformContext, area, finalRomName, mediaAssetType, sourceMedia.extension());
            if (!brokenMediaTargets.contains(normalizedPath(targetPath))) {
                return sourceMedia;
            }
            return japanCandidates.stream()
                    .filter(candidate -> !normalizedPath(candidate.path()).equals(normalizedPath(sourceMedia.path())))
                    .findFirst()
                    .orElse(null);
        }

        var sourceMedia = selectSourceMedia(candidates, area, spec, Set.of());
        if (sourceMedia == null || !sourceMedia.primarySourceRoot()) {
            return sourceMedia;
        }

        var targetPath = mediaTargetPath(platformContext, area, finalRomName, mediaAssetType, sourceMedia.extension());
        if (!brokenMediaTargets.contains(normalizedPath(targetPath))) {
            return sourceMedia;
        }
        return selectSourceMedia(candidates, area, spec, Set.of(normalizedPath(sourceMedia.path())));
    }

    private SourceMedia selectSourceMedia(List<SourceMedia> candidates,
                                          String area,
                                          SourceMediaSpec spec,
                                          Set<Path> excludedSourcePaths) {
        var availableCandidates = candidates.stream()
                .filter(sourceMedia -> !excludedSourcePaths.contains(normalizedPath(sourceMedia.path())))
                .toList();
        if (availableCandidates.isEmpty()) {
            return null;
        }
        if (!spec.areaSpecific()) {
            return availableCandidates.get(0);
        }

        var priorityRegions = priorityRegions(area);
        for (var region : priorityRegions) {
            var sourceMedia = findFirstSourceMediaByRegion(availableCandidates, region);
            if (sourceMedia != null) {
                return sourceMedia;
            }
        }

        var arbitraryRegionCandidates = availableCandidates.stream()
                .filter(sourceMedia -> !JAPAN_REGION.equals(sourceMedia.region()))
                .filter(sourceMedia -> !priorityRegions.contains(sourceMedia.region()))
                .toList();
        if (arbitraryRegionCandidates.isEmpty()) {
            return findFirstSourceMediaByRegion(availableCandidates, JAPAN_REGION);
        }
        return arbitraryRegionCandidates.get(ThreadLocalRandom.current().nextInt(arbitraryRegionCandidates.size()));
    }

    private List<String> priorityRegions(String area) {
        var regions = new ArrayList<String>();
        switch (area) {
            case JAPAN_REGION -> {
                addRegion(regions, JAPAN_REGION);
                addRegion(regions, WORLD_REGION);
                addRegion(regions, SPECIAL_REGION);
                addRegion(regions, USA_REGION);
                addRegion(regions, EUROPE_REGION);
            }
            case USA_REGION -> {
                addRegion(regions, USA_REGION);
                addRegion(regions, WORLD_REGION);
                addRegion(regions, EUROPE_REGION);
                addRegion(regions, SPECIAL_REGION);
            }
            default -> {
                addRegion(regions, area);
                addRegion(regions, EUROPE_REGION);
                addRegion(regions, WORLD_REGION);
                addRegion(regions, USA_REGION);
                addRegion(regions, SPECIAL_REGION);
            }
        }
        return regions;
    }

    private void addRegion(List<String> regions, String region) {
        if (!regions.contains(region)) {
            regions.add(region);
        }
    }

    private SourceMedia findFirstSourceMediaByRegion(List<SourceMedia> sourceMediaList, String region) {
        return sourceMediaList.stream()
                .filter(sourceMedia -> region.equals(sourceMedia.region()))
                .findFirst()
                .orElse(null);
    }

    private Path mediaTargetPath(PlatformContext platformContext,
                                 String area,
                                 String finalRomName,
                                 MediaAssetType mediaAssetType,
                                 String extension) {
        return PathUtils.esdeMedia(
                platformContext,
                mediaAssetType,
                PathUtils.esdeAreaDirectoryName(platformContext, area),
                finalRomName,
                extension);
    }

    private Path normalizedPath(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private Map<String, Map<MediaAssetType, MediaCompletionRate>> populateMediaBitmapsAndBuildCompletionRates(
            PlatformContext platformContext,
            Map<String, Map<String, FinalGame>> finalGameMapByArea) {
        resetMediaBitmaps(finalGameMapByArea);

        var mediaCompletionRateMap = new LinkedHashMap<String, Map<MediaAssetType, MediaCompletionRate>>();
        finalGameMapByArea.forEach((area, finalGames) -> {
            var rates = new LinkedHashMap<MediaAssetType, MediaCompletionRate>();
            var mediaAreaDirectoryName = PathUtils.esdeAreaDirectoryName(platformContext, area);
            for (var finalGame : finalGames.values()) {
                var mediaBitmap = 0;
                for (var mediaAssetType : MediaAssetType.values()) {
                    if (existsMediaFile(platformContext, mediaAssetType, mediaAreaDirectoryName, finalGame.getFinalRomName())) {
                        mediaBitmap = MediaBitmapUtils.withMedia(mediaBitmap, mediaAssetType);
                    }
                }
                finalGame.setMediaBitMap(mediaBitmap);
            }

            for (var mediaAssetType : MediaAssetType.values()) {
                var completedCount = (int) finalGames.values().stream()
                        .filter(finalGame -> MediaBitmapUtils.hasMedia(finalGame.getMediaBitMap(), mediaAssetType))
                        .count();
                rates.put(mediaAssetType, MediaCompletionRate.of(finalGames.size(), completedCount));
            }
            mediaCompletionRateMap.put(area, rates);
        });
        return mediaCompletionRateMap;
    }

    private boolean existsMediaFile(PlatformContext platformContext,
                                    MediaAssetType mediaAssetType,
                                    String mediaAreaDirectoryName,
                                    String finalName) {
        var primaryPath = PathUtils.esdeMedia(
                platformContext,
                mediaAssetType,
                mediaAreaDirectoryName,
                finalName,
                mediaAssetType.getPrimaryExtension());
        if (Files.isRegularFile(primaryPath)) {
            return true;
        }

        var fallbackExtension = mediaAssetType.getFallbackExtension();
        return fallbackExtension != null && Files.isRegularFile(PathUtils.esdeMedia(
                platformContext,
                mediaAssetType,
                mediaAreaDirectoryName,
                finalName,
                fallbackExtension));
    }

    private record SourceMediaSpec(List<String> ssMediaTypes, boolean areaSpecific) {

        private SourceMediaSpec(String ssMediaType, boolean areaSpecific) {
            this(List.of(ssMediaType), areaSpecific);
        }
    }

    private record MediaCatalog(Map<String, Map<MediaAssetType, List<SourceMedia>>> mediaByPackageIdAndType,
                                Set<Path> brokenMediaTargets) {
    }

    private record SourceMedia(Path path,
                               String extension,
                               String region,
                               MediaAssetType mediaAssetType,
                               int sourceMediaTypeRank,
                               int sourceRootRank,
                               boolean primarySourceRoot) {
    }

    private record CopyItem(Path sourcePath, Path targetPath) {
    }
}

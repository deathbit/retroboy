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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/*
 * 媒体匹配策略：
 *
 * fanart、video：
 *   只匹配 NULL 地区。
 *
 * manuel：
 *   JPN：JPN
 *   USA：USA > WOR
 *   其它：本身地区 > EUR > WOR > USA
 *
 * box-2D、box-2D-back、box-3D：
 *   JPN：JPN > SS
 *   USA：USA > WOR > SS
 *   其它：本身地区 > EUR > WOR > SS
 *
 * support-2D：
 *   JPN：JPN
 *   USA：USA > WOR
 *   其它：本身地区 > EUR > WOR > 除了 USA 和 JPN 以外的任一地区 > USA
 *
 * ss、sstitle、wheel：
 *   JPN：JPN > WOR > USA > EUR > 任意其它地区
 *   USA：USA > WOR > EUR > JPN > 任意其它地区
 *   其它：本身地区 > EUR > WOR > USA > JPN > 任意其它地区
 *
 * marquees：
 *   先按 wheel 执行 ss/sstitle/wheel 规则；未命中时再按 wheel-hd 执行同样规则。
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

        deleteBrokenMedia(platformContext);
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
                                          Map<String, Map<String, FinalGame>> finalGameMapByArea) {
        if (platformContext.getMatchResults() == null || platformContext.getMatchResults().isEmpty()) {
            throw new IllegalStateException("matchResults is empty, run FileContextToSSGamePackageMatchHandler before MediaHandler");
        }

        var copyItems = new ArrayList<CopyItem>();
        for (var matchResult : platformContext.getMatchResults()) {
            collectCopyItems(platformContext, matchResult, finalGameMapByArea, copyItems);
        }
        return copyItems;
    }

    private void collectCopyItems(PlatformContext platformContext,
                                  MatchResult matchResult,
                                  Map<String, Map<String, FinalGame>> finalGameMapByArea,
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
            collectCopyItems(platformContext, area, finalRomName, ssGamePackage, copyItems);
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
                                  List<CopyItem> copyItems) {
        SOURCE_MEDIA_SPECS.forEach((mediaAssetType, spec) -> {
            var sourceMedia = findSourceMedia(platformContext, ssGamePackage.getId(), area, mediaAssetType, spec);
            var targetPath = PathUtils.esdeMedia(
                    platformContext,
                    mediaAssetType,
                    PathUtils.esdeAreaDirectoryName(platformContext, area),
                    finalRomName,
                    sourceMedia.extension());
            copyItems.add(new CopyItem(sourceMedia.path(), targetPath));
        });
    }

    private SourceMedia findSourceMedia(PlatformContext platformContext,
                                        String packageId,
                                        String area,
                                        MediaAssetType mediaAssetType,
                                        SourceMediaSpec spec) {
        var sourceDirectory = PathUtils.PLATFORM_RESOURCE_ROOT.get(platformContext)
                .resolve("ss")
                .resolve(packageId);
        var region = spec.areaSpecific() ? area : "NULL";

        SourceMedia firstSourceMedia = null;
        for (var ssMediaType : spec.ssMediaTypes()) {
            var sourceMedia = findSourceMedia(sourceDirectory, packageId, region, mediaAssetType, ssMediaType);
            if (firstSourceMedia == null) {
                firstSourceMedia = sourceMedia;
            }
            if (Files.exists(sourceMedia.path())) {
                return sourceMedia;
            }

            for (var fallbackRegion : fallbackRegions(mediaAssetType, region)) {
                if (fallbackRegion.equals(region)) {
                    continue;
                }
                var fallbackSourceMedia = findSourceMedia(sourceDirectory, packageId, fallbackRegion, mediaAssetType, ssMediaType);
                if (Files.exists(fallbackSourceMedia.path())) {
                    return fallbackSourceMedia;
                }
            }

            if (usesAnyRegionFallback(mediaAssetType)) {
                var anyRegionSourceMedia = findAnyRegionSourceMedia(sourceDirectory, packageId, mediaAssetType, ssMediaType, List.of());
                if (anyRegionSourceMedia != null) {
                    return anyRegionSourceMedia;
                }
            }

            if (usesPhysicalMediaExtraFallback(mediaAssetType, region)) {
                var anyRegionSourceMedia = findAnyRegionSourceMedia(
                        sourceDirectory,
                        packageId,
                        mediaAssetType,
                        ssMediaType,
                        List.of(USA_REGION, JAPAN_REGION));
                if (anyRegionSourceMedia != null) {
                    return anyRegionSourceMedia;
                }

                var usaSourceMedia = findSourceMedia(sourceDirectory, packageId, USA_REGION, mediaAssetType, ssMediaType);
                if (Files.exists(usaSourceMedia.path())) {
                    return usaSourceMedia;
                }
            }
        }
        return firstSourceMedia;
    }

    private SourceMedia findSourceMedia(Path sourceDirectory,
                                        String packageId,
                                        String region,
                                        MediaAssetType mediaAssetType,
                                        String ssMediaType) {
        var fileNameWithoutExtension = String.join("_", packageId, ssMediaType, region)
                .toUpperCase(Locale.ROOT);
        var primarySource = sourceDirectory.resolve(fileNameWithoutExtension + "." + mediaAssetType.getPrimaryExtension());
        if (mediaAssetType.getFallbackExtension() == null || Files.exists(primarySource)) {
            return new SourceMedia(primarySource, mediaAssetType.getPrimaryExtension());
        }
        return new SourceMedia(
                sourceDirectory.resolve(fileNameWithoutExtension + "." + mediaAssetType.getFallbackExtension()),
                mediaAssetType.getFallbackExtension());
    }

    private SourceMedia findAnyRegionSourceMedia(Path sourceDirectory,
                                                 String packageId,
                                                 MediaAssetType mediaAssetType,
                                                 String ssMediaType,
                                                 List<String> excludedRegions) {
        if (Files.notExists(sourceDirectory)) {
            return null;
        }

        var fileNamePrefix = (packageId + "_" + ssMediaType + "_").toUpperCase(Locale.ROOT);
        try (var stream = Files.list(sourceDirectory)) {
            var candidates = stream.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName -> isAnyRegionSourceMedia(fileName, fileNamePrefix, mediaAssetType, excludedRegions))
                    .toList();
            if (candidates.isEmpty()) {
                return null;
            }

            var fileName = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            return new SourceMedia(sourceDirectory.resolve(fileName), extension(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isAnyRegionSourceMedia(String fileName,
                                           String fileNamePrefix,
                                           MediaAssetType mediaAssetType,
                                           List<String> excludedRegions) {
        var normalizedFileName = fileName.toUpperCase(Locale.ROOT);
        return normalizedFileName.startsWith(fileNamePrefix)
                && hasSupportedExtension(normalizedFileName, mediaAssetType)
                && !excludedRegions.contains(region(fileName, fileNamePrefix));
    }

    private boolean hasSupportedExtension(String normalizedFileName, MediaAssetType mediaAssetType) {
        if (normalizedFileName.endsWith("." + mediaAssetType.getPrimaryExtension().toUpperCase(Locale.ROOT))) {
            return true;
        }

        var fallbackExtension = mediaAssetType.getFallbackExtension();
        return fallbackExtension != null
                && normalizedFileName.endsWith("." + fallbackExtension.toUpperCase(Locale.ROOT));
    }

    private String extension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new IllegalStateException("Media file extension not found: " + fileName);
        }
        return fileName.substring(dotIndex + 1);
    }

    private String region(String fileName, String fileNamePrefix) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalStateException("Media file extension not found: " + fileName);
        }
        return fileName.substring(fileNamePrefix.length(), dotIndex).toUpperCase(Locale.ROOT);
    }

    private boolean usesAnyRegionFallback(MediaAssetType mediaAssetType) {
        return mediaAssetType == MediaAssetType.SCREENSHOT
                || mediaAssetType == MediaAssetType.TITLE_SCREEN
                || mediaAssetType == MediaAssetType.MARQUEE;
    }

    private boolean usesPhysicalMediaExtraFallback(MediaAssetType mediaAssetType, String region) {
        return mediaAssetType == MediaAssetType.PHYSICAL_MEDIA
                && !JAPAN_REGION.equals(region)
                && !USA_REGION.equals(region);
    }

    private List<String> fallbackRegions(MediaAssetType mediaAssetType, String region) {
        return switch (mediaAssetType) {
            case MANUAL -> manualFallbackRegions(region);
            case PHYSICAL_MEDIA -> physicalMediaFallbackRegions(region);
            case THREE_D_BOX, BACK_COVER, COVER -> boxFallbackRegions(region);
            case SCREENSHOT, TITLE_SCREEN, MARQUEE -> imageFallbackRegions(region);
            case FANART, MIX_IMAGE, VIDEO -> List.of();
        };
    }

    private List<String> manualFallbackRegions(String region) {
        return switch (region) {
            case JAPAN_REGION -> List.of();
            case USA_REGION -> List.of(WORLD_REGION);
            default -> List.of(EUROPE_REGION, WORLD_REGION, USA_REGION);
        };
    }

    private List<String> physicalMediaFallbackRegions(String region) {
        return switch (region) {
            case JAPAN_REGION -> List.of();
            case USA_REGION -> List.of(WORLD_REGION);
            default -> List.of(EUROPE_REGION, WORLD_REGION);
        };
    }

    private List<String> boxFallbackRegions(String region) {
        return switch (region) {
            case JAPAN_REGION -> List.of(SPECIAL_REGION);
            case USA_REGION -> List.of(WORLD_REGION, SPECIAL_REGION);
            default -> List.of(EUROPE_REGION, WORLD_REGION, SPECIAL_REGION);
        };
    }

    private List<String> imageFallbackRegions(String region) {
        return switch (region) {
            case JAPAN_REGION -> List.of(WORLD_REGION, USA_REGION, EUROPE_REGION);
            case USA_REGION -> List.of(WORLD_REGION, EUROPE_REGION, JAPAN_REGION);
            default -> List.of(EUROPE_REGION, WORLD_REGION, USA_REGION, JAPAN_REGION);
        };
    }

    private void deleteBrokenMedia(PlatformContext platformContext) {
        var brokenMediaList = platformContext.getPlatformPackTaskConfig().getBrokenMediaList();
        if (brokenMediaList == null || brokenMediaList.isEmpty()) {
            return;
        }

        for (var brokenMedia : brokenMediaList) {
            var path = brokenMedia.isAbsolute()
                    ? brokenMedia
                    : PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext).resolve(brokenMedia);
            fileComponent.deletePath(path);
        }
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

    private record SourceMedia(Path path, String extension) {
    }

    private record CopyItem(Path sourcePath, Path targetPath) {
    }
}

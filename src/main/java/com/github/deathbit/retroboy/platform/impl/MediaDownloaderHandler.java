package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.SSMedia;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;

@Component
public class MediaDownloaderHandler {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Set<String> SUPPORTED_MEDIA_TYPES = Set.of(
            "box-3D",
            "box-2D",
            "manuel",
            "ss",
            "video",
            "box-2D-back",
            "fanart",
            "wheel",
            "wheel-hd",
            "support-2D",
            "sstitle"
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public void handle(PlatformContext platformContext) throws Exception {
        var downloadItems = buildDownloadItems(platformContext);
        var processedTargets = new LinkedHashSet<Path>();
        var ssid = SSHandler.configuredSsid(platformContext);

        ProgressBar pb = new ProgressBar("下载ScreenScraper媒体");
        pb.startTask(downloadItems.size());
        for (int i = 0; i < downloadItems.size(); i++) {
            var downloadItem = downloadItems.get(i);
            if (processedTargets.add(downloadItem.targetPath()) && Files.notExists(downloadItem.targetPath())) {
                download(downloadItem, ssid);
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    private List<DownloadItem> buildDownloadItems(PlatformContext platformContext) {
        if (platformContext.getMatchResults() == null || platformContext.getMatchResults().isEmpty()) {
            throw new IllegalStateException("matchResults is empty, run FileContextToSSGamePackageMatchHandler before MediaDownloaderHandler");
        }

        var downloadItems = new ArrayList<DownloadItem>();
        for (var matchResult : platformContext.getMatchResults()) {
            collectDownloadItems(platformContext, matchResult, downloadItems);
        }
        return downloadItems;
    }

    private void collectDownloadItems(PlatformContext platformContext,
                                      MatchResult matchResult,
                                      List<DownloadItem> downloadItems) {
        if (matchResult.getSsGamePackageByArea() == null || matchResult.getSsGamePackageByArea().isEmpty()) {
            return;
        }

        for (var ssGamePackage : matchResult.getSsGamePackageByArea().values()) {
            collectDownloadItems(platformContext, ssGamePackage, downloadItems);
        }
    }

    private void collectDownloadItems(PlatformContext platformContext,
                                      SSGamePackage ssGamePackage,
                                      List<DownloadItem> downloadItems) {
        if (ssGamePackage == null || ssGamePackage.getMediaByType() == null || ssGamePackage.getMediaByType().isEmpty()) {
            return;
        }

        var packageId = valueOrNull(ssGamePackage.getId());
        var packageDirectory = PathUtils.PLATFORM_RESOURCE_ROOT.get(platformContext)
                .resolve("ss")
                .resolve(packageId);
        for (var mediaType : SUPPORTED_MEDIA_TYPES) {
            var medias = ssGamePackage.getMediaByType().get(mediaType);
            if (medias == null || medias.isEmpty()) {
                continue;
            }
            for (var media : medias) {
                var fileName = buildFileName(ssGamePackage, mediaType, media);
                downloadItems.add(new DownloadItem(packageDirectory.resolve(fileName), media == null ? null : media.getUrl()));
            }
        }
    }

    private String buildFileName(SSGamePackage ssGamePackage, String mediaType, SSMedia media) {
        var baseName = String.join("_",
                valueOrNull(ssGamePackage.getId()),
                valueOrNull(mediaType),
                valueOrNull(mapMediaRegion(media))
        ).toUpperCase(Locale.ROOT);
        var extension = valueOrNull(media == null ? null : media.getFormat());
        if (!"NULL".equals(extension)) {
            extension = extension.toLowerCase(Locale.ROOT);
        }
        return baseName + "." + extension;
    }

    private String mapMediaRegion(SSMedia media) {
        if (media == null || media.getRegion() == null) {
            return null;
        }
        return SSHandler.mapMediaRegionToArea(media.getRegion());
    }

    private String valueOrNull(String value) {
        return value == null ? "NULL" : value;
    }

    private void download(DownloadItem downloadItem, String ssid) throws Exception {
        if (downloadItem.url() == null) {
            throw new IllegalStateException("ScreenScraper媒体URL为空: " + downloadItem.targetPath());
        }
        var url = withConfiguredSsid(downloadItem.url(), ssid);

        Files.createDirectories(downloadItem.targetPath().getParent());
        var tempPath = downloadItem.targetPath().resolveSibling(downloadItem.targetPath().getFileName() + ".tmp");
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tempPath));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("ScreenScraper媒体下载失败: url=%s, httpStatus=%s, target=%s"
                        .formatted(url, response.statusCode(), downloadItem.targetPath()));
            }
            Files.move(tempPath, downloadItem.targetPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    private String withConfiguredSsid(String url, String ssid) {
        var encodedSsid = URLEncoder.encode(ssid, StandardCharsets.UTF_8);
        if (url.contains("ssid=")) {
            return url.replaceFirst("([?&]ssid=)[^&]*", "$1" + Matcher.quoteReplacement(encodedSsid));
        }
        return url + (url.contains("?") ? "&" : "?") + "ssid=" + encodedSsid;
    }

    private record DownloadItem(Path targetPath, String url) {
    }
}

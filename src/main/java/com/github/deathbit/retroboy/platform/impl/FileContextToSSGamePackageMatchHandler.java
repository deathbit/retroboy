package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.MatchPairForGame;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class FileContextToSSGamePackageMatchHandler {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    public void handle(PlatformContext platformContext) {
        var matchResults = requireMatchResults(platformContext);
        var ssGamePackagesBySha1 = buildSSGamePackagesBySha1(platformContext.getSsGamePackages());
        var areaMapping = getPlatformProcessor(platformContext).gameDBToWikiDBAreaMapping(
            buildGameAreas(platformContext.getNoIntroGamePackages()), buildWikiAreas(platformContext.getWikiGamePackages()));

        for (var matchResult : matchResults) {
            var fileContextByArea = requireFileContextByArea(matchResult);
            var matchPairForGameByArea = new LinkedHashMap<String, MatchPairForGame>();
            var ssGamePackageByArea = new LinkedHashMap<String, SSGamePackage>();

            for (var entry : matchResult.getNoIntroGamePackage().getNoIntroGameByArea().entrySet()) {
                var noIntroArea = entry.getKey();
                var noIntroGame = entry.getValue();
                var wikiArea = areaMapping.getOrDefault(noIntroArea, noIntroArea);
                var wikiGame = matchResult.getWikiGamePackage().getWikiGameByArea().get(wikiArea);
                if (wikiGame == null) {
                    throw new RuntimeException("MatchResult 缺少对应 WikiDB: wikiPackageId=%s, gamePackageId=%s, gameArea=%s, wikiArea=%s"
                        .formatted(matchResult.getWikiGamePackage().getId(), matchResult.getNoIntroGamePackage().getId(), noIntroArea, wikiArea));
                }

                var fileContext = fileContextByArea.get(noIntroArea);
                if (fileContext == null) {
                    throw new RuntimeException("MatchResult 缺少对应 FileContext: wikiPackageId=%s, gamePackageId=%s, gameArea=%s"
                        .formatted(matchResult.getWikiGamePackage().getId(), matchResult.getNoIntroGamePackage().getId(), noIntroArea));
                }
                var ssGamePackage = requireSingleSSGamePackage(ssGamePackagesBySha1, fileContext, noIntroArea, noIntroGame.getTitle());

                matchPairForGameByArea.put(noIntroArea, MatchPairForGame.builder()
                                                                        .wikiGame(wikiGame)
                                                                        .noIntroGame(noIntroGame)
                                                                        .build());
                ssGamePackageByArea.put(noIntroArea, ssGamePackage);
            }

            validateSingleSSGamePackageByArea(platformContext, matchResult, matchPairForGameByArea, fileContextByArea, ssGamePackageByArea);
            matchResult.setSsGamePackageByArea(ssGamePackageByArea);
        }
    }

    private List<MatchResult> requireMatchResults(PlatformContext platformContext) {
        var matchResults = platformContext.getMatchResults();
        if (matchResults == null) {
            throw new RuntimeException("matchResults is null, run WikiGamePackageToNoIntroGamePackageMatchHandler before FileContextToSSGamePackageMatchHandler");
        }
        return matchResults;
    }

    private Map<String, FileContext> requireFileContextByArea(MatchResult matchResult) {
        var fileContextByArea = matchResult.getFileContextByArea();
        if (fileContextByArea == null) {
            throw new RuntimeException("fileContextByArea is null, run NoIntroGamePackageToFileContextMatchHandler before FileContextToSSGamePackageMatchHandler: wikiPackageId=%s, gamePackageId=%s"
                .formatted(matchResult.getWikiGamePackage().getId(), matchResult.getNoIntroGamePackage().getId()));
        }
        return fileContextByArea;
    }

    private List<String> buildWikiAreas(List<WikiGamePackage> wikiGamePackages) {
        var areas = new LinkedHashSet<String>();
        for (var pkg : wikiGamePackages) {
            areas.addAll(pkg.getWikiGameByArea().keySet());
        }
        return new ArrayList<>(areas);
    }

    private List<String> buildGameAreas(List<NoIntroGamePackage> noIntroGamePackages) {
        var areas = new LinkedHashSet<String>();
        for (var pkg : noIntroGamePackages) {
            areas.addAll(pkg.getNoIntroGameByArea().keySet());
        }
        return new ArrayList<>(areas);
    }

    private void validateSingleSSGamePackageByArea(
        PlatformContext platformContext,
        MatchResult matchResult,
        Map<String, MatchPairForGame> matchPairForGameByArea,
        Map<String, FileContext> fileContextByArea,
        Map<String, SSGamePackage> ssGamePackageByArea
    ) {
        var allowList = platformContext.getPlatformPackTaskConfig().getAllowDifferentSSGamePackageWikiIds();
        if (allowList != null && allowList.contains(matchResult.getWikiGamePackage().getId())) {
            return;
        }

        String expectedPackageId = null;
        var actualPackageDetailsByArea = new LinkedHashMap<String, Map<String, Object>>();
        for (var entry : ssGamePackageByArea.entrySet()) {
            var area = entry.getKey();
            var ssGamePackage = entry.getValue();
            var packageId = ssGamePackage.getId();
            actualPackageDetailsByArea.put(area, buildSSGamePackageConflictDetail(
                matchPairForGameByArea.get(area),
                fileContextByArea.get(area),
                ssGamePackage
            ));
            if (expectedPackageId == null) {
                expectedPackageId = packageId;
                continue;
            }
            if (!expectedPackageId.equals(packageId)) {
                throw new RuntimeException("MatchResult SSGamePackage area conflict:\n" + buildSSGamePackageConflictJson(matchResult, actualPackageDetailsByArea));
            }
        }
    }

    private String buildSSGamePackageConflictJson(
        MatchResult matchResult,
        Map<String, Map<String, Object>> detailsByArea
    ) {
        var report = new LinkedHashMap<String, Object>();
        report.put("wikiPackageId", matchResult.getWikiGamePackage().getId());
        report.put("gamePackageId", matchResult.getNoIntroGamePackage().getId());
        report.put("detailsByArea", detailsByArea);
        return GSON.toJson(report);
    }

    private Map<String, Object> buildSSGamePackageConflictDetail(
        MatchPairForGame matchPairForGame,
        FileContext fileContext,
        SSGamePackage ssGamePackage
    ) {
        var detail = new LinkedHashMap<String, Object>();
        detail.put("wikiGame", matchPairForGame == null ? null : matchPairForGame.getWikiGame().getTitle());
        detail.put("noIntroGame", matchPairForGame == null ? null : matchPairForGame.getNoIntroGame().getTitle());
        detail.put("fileName", fileContext == null ? null : fileContext.getFileName());
        detail.put("sha1", fileContext == null ? null : fileContext.getSha1());
        detail.put("ssPackageId", ssGamePackage == null ? null : ssGamePackage.getId());
        detail.put("ssGames", buildSSGameNamesByArea(ssGamePackage));
        return detail;
    }

    private Map<String, String> buildSSGameNamesByArea(SSGamePackage ssGamePackage) {
        var ssGameNamesByArea = new LinkedHashMap<String, String>();
        if (ssGamePackage == null || ssGamePackage.getSsGameByArea() == null) {
            return ssGameNamesByArea;
        }
        ssGamePackage.getSsGameByArea().forEach((area, ssGame) -> ssGameNamesByArea.put(area, ssGame.getTitle()));
        return ssGameNamesByArea;
    }

    private Map<String, List<SSGamePackage>> buildSSGamePackagesBySha1(List<SSGamePackage> ssGamePackages) {
        var lookupMap = new LinkedHashMap<String, List<SSGamePackage>>();
        if (ssGamePackages == null) {
            return lookupMap;
        }
        for (var ssGamePackage : ssGamePackages) {
            if (ssGamePackage.getSha1s() == null) {
                continue;
            }
            for (var sha1 : ssGamePackage.getSha1s()) {
                if (sha1 == null || sha1.isBlank()) {
                    continue;
                }
                lookupMap.computeIfAbsent(normalizeSha1(sha1), ignored -> new ArrayList<>()).add(ssGamePackage);
            }
        }
        return lookupMap;
    }

    private SSGamePackage requireSingleSSGamePackage(
        Map<String, List<SSGamePackage>> ssGamePackagesBySha1,
        FileContext fileContext,
        String area,
        String noIntroTitle
    ) {
        var sha1 = fileContext.getSha1();
        if (sha1 == null || sha1.isBlank()) {
            throw new RuntimeException("FileContext 缺少 SHA1: area=%s, rom=%s, file=%s"
                .formatted(area, noIntroTitle, fileContext.getFileName()));
        }

        var candidates = ssGamePackagesBySha1.get(normalizeSha1(sha1));
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("SSGamePackage not found: area=%s, rom=%s, file=%s, sha1=%s"
                .formatted(area, noIntroTitle, fileContext.getFileName(), sha1));
        }
        if (candidates.size() > 1) {
            throw new RuntimeException("Multiple SSGamePackages found: area=%s, rom=%s, file=%s, sha1=%s, ssPackageIds=%s"
                .formatted(area, noIntroTitle, fileContext.getFileName(), sha1, candidates.stream().map(SSGamePackage::getId).toList()));
        }
        return candidates.get(0);
    }

    private String normalizeSha1(String sha1) {
        return sha1.trim().toUpperCase(Locale.ROOT);
    }

    private PlatformProcessor getPlatformProcessor(PlatformContext platformContext) {
        var platformProcessor = platformProcessorMap.get(platformContext.getPlatform());
        if (platformProcessor == null) {
            throw new IllegalStateException("PlatformProcessor not found for platform: " + platformContext.getPlatform());
        }
        return platformProcessor;
    }
}


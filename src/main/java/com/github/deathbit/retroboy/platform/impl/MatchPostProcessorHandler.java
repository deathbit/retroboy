package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class MatchPostProcessorHandler {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public void handle(PlatformContext platformContext) {
        if (platformContext.getMatchResults() == null || platformContext.getMatchResults().isEmpty()) {
            throw new IllegalStateException("matchResults is empty, run MatchHandler before MatchPostProcessorHandler");
        }

        var releaseDateConfig = parseReleaseDateConfig(platformContext);
        applyReleaseDateConfig(platformContext.getMatchResults(), releaseDateConfig);
        validateReleaseDateByArea(platformContext.getMatchResults());
    }

    private Map<String, String> parseReleaseDateConfig(PlatformContext platformContext) {
        var configList = platformContext.getPlatformPackTaskConfig().getReleaseDateConfig();
        if (configList == null || configList.isEmpty()) {
            return Map.of();
        }

        var config = new LinkedHashMap<String, String>();
        for (var item : configList) {
            var parts = item.split("\\s+-\\s+", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("releaseDateConfig 格式错误: " + item);
            }

            var key = normalizeReleaseDateConfigKey(parts[0]);
            var releaseDate = parts[1].trim();
            if (releaseDate.isEmpty()) {
                throw new IllegalArgumentException("releaseDateConfig 发售日不能为空: " + item);
            }
            var previous = config.putIfAbsent(key, releaseDate);
            if (previous != null) {
                throw new IllegalArgumentException("releaseDateConfig 重复: " + key);
            }
        }
        return config;
    }

    private String normalizeReleaseDateConfigKey(String key) {
        var trimmedKey = key.trim();
        var separatorIndex = trimmedKey.lastIndexOf('.');
        if (separatorIndex <= 0 || separatorIndex == trimmedKey.length() - 1) {
            throw new IllegalArgumentException("releaseDateConfig key 格式错误: " + key);
        }
        return trimmedKey.substring(0, separatorIndex).trim()
                + "."
                + trimmedKey.substring(separatorIndex + 1).trim().toUpperCase(Locale.ROOT);
    }

    private void applyReleaseDateConfig(List<MatchResult> matchResults, Map<String, String> releaseDateConfig) {
        for (var matchResult : matchResults) {
            if (matchResult.getSsGamePackageByArea() == null) {
                continue;
            }

            for (var entry : matchResult.getSsGamePackageByArea().entrySet()) {
                var area = entry.getKey();
                var ssGamePackage = entry.getValue();
                if (ssGamePackage == null || hasReleaseDate(ssGamePackage, area)) {
                    continue;
                }

                var releaseDate = releaseDateConfig.get(buildReleaseDateConfigKey(ssGamePackage, area));
                if (releaseDate != null) {
                    putReleaseDate(ssGamePackage, area, releaseDate);
                }
            }
        }
    }

    private void validateReleaseDateByArea(List<MatchResult> matchResults) {
        var missingReleaseDateReports = new ArrayList<Map<String, Object>>();
        for (var matchResult : matchResults) {
            if (matchResult.getSsGamePackageByArea() == null) {
                continue;
            }
            for (var entry : matchResult.getSsGamePackageByArea().entrySet()) {
                var area = entry.getKey();
                var ssGamePackage = entry.getValue();
                if (ssGamePackage != null && hasReleaseDate(ssGamePackage, area)) {
                    continue;
                }

                var report = new LinkedHashMap<String, Object>();
                report.put("wikiPackageId", matchResult.getWikiGamePackage() == null ? null : matchResult.getWikiGamePackage().getId());
                report.put("gamePackageId", matchResult.getNoIntroGamePackage() == null ? null : matchResult.getNoIntroGamePackage().getId());
                report.put("area", area);
                report.put("wikiReleaseDate", findWikiReleaseDate(matchResult, area));
                report.put("ssPackageId", ssGamePackage == null ? null : ssGamePackage.getId());
                report.put("ssReleaseDateByArea", ssGamePackage == null ? null : ssGamePackage.getReleaseDateByArea());
                missingReleaseDateReports.add(report);
            }
        }

        if (!missingReleaseDateReports.isEmpty()) {
            var report = new LinkedHashMap<String, Object>();
            report.put("count", missingReleaseDateReports.size());
            report.put("items", missingReleaseDateReports);
            throw new RuntimeException("MatchPostProcessor releaseDate missing for matched area: count=%s\n%s\n%s"
                    .formatted(
                            missingReleaseDateReports.size(),
                            GSON.toJson(report),
                            buildMissingReleaseDateSupplementLog(missingReleaseDateReports)
                    ));
        }
    }

    private boolean hasReleaseDate(SSGamePackage ssGamePackage, String area) {
        return ssGamePackage.getReleaseDateByArea() != null
                && ssGamePackage.getReleaseDateByArea().get(area) != null
                && !ssGamePackage.getReleaseDateByArea().get(area).isBlank();
    }

    private void putReleaseDate(SSGamePackage ssGamePackage, String area, String releaseDate) {
        if (ssGamePackage.getReleaseDateByArea() == null) {
            ssGamePackage.setReleaseDateByArea(new LinkedHashMap<>());
        }
        ssGamePackage.getReleaseDateByArea().put(area, releaseDate);
        if (ssGamePackage.getSsGameByArea() != null && ssGamePackage.getSsGameByArea().get(area) != null) {
            ssGamePackage.getSsGameByArea().get(area).setReleaseDate(releaseDate);
        }
    }

    private String buildReleaseDateConfigKey(SSGamePackage ssGamePackage, String area) {
        return ssGamePackage.getId() + "." + area.toUpperCase(Locale.ROOT);
    }

    private String buildMissingReleaseDateSupplementLog(List<Map<String, Object>> reports) {
        var lines = new StringBuilder();
        for (var report : reports) {
            lines.append("- \"")
                    .append(report.get("ssPackageId"))
                    .append(".")
                    .append(report.get("area"))
                    .append(" - ")
                    .append(report.get("wikiReleaseDate"))
                    .append("\"")
                    .append(System.lineSeparator());
        }
        return lines.toString();
    }

    private String findWikiReleaseDate(MatchResult matchResult, String area) {
        if (matchResult.getWikiGamePackage() == null || matchResult.getWikiGamePackage().getWikiGameByArea() == null) {
            return null;
        }
        var wikiGame = matchResult.getWikiGamePackage().getWikiGameByArea().get(area);
        return wikiGame == null ? null : wikiGame.getReleaseDate();
    }
}

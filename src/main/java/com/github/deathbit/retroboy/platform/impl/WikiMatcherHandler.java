package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.AreaRenameResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.WikiGameEntry;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class WikiMatcherHandler {

    private static final Pattern AREA_HEADER_PATTERN = Pattern.compile("^([A-Z]+)\\((\\d+)\\):$");
    private static final String SEPARATOR = " || ";
    private static final String UNMATCHED_MARKER = "=====";
    public void handle(PlatformContext platformContext) {
        var wikiRomPath = PathUtils.PLATFORM_WIKI_ROM_MAPPING.get(platformContext);
        var finalNameIndex = buildFinalNameIndex(platformContext);
        var areaWikiEntryMap = new LinkedHashMap<Area, Map<String, WikiGameEntry>>();

        Area currentArea = null;
        int expectedCount = 0;
        int actualCount = 0;

        try {
            var lines = Files.readAllLines(wikiRomPath, StandardCharsets.UTF_8);
            for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
                var line = lines.get(lineNumber - 1).trim();
                if (line.isEmpty()) {
                    continue;
                }

                var headerMatcher = AREA_HEADER_PATTERN.matcher(line);
                if (headerMatcher.matches()) {
                    validateAreaCount(currentArea, expectedCount, actualCount, lineNumber);
                    currentArea = parseArea(headerMatcher.group(1), lineNumber);
                    expectedCount = Integer.parseInt(headerMatcher.group(2));
                    actualCount = 0;
                    areaWikiEntryMap.put(currentArea, new LinkedHashMap<>());
                    continue;
                }

                if (currentArea == null) {
                    throw new IllegalArgumentException("Wiki mapping entry found before area header at line " + lineNumber);
                }

                var separatorIndex = line.indexOf(SEPARATOR);
                if (separatorIndex == -1) {
                    throw new IllegalArgumentException("Invalid wiki mapping format at line " + lineNumber + ": " + line);
                }

                var wikiName = line.substring(0, separatorIndex).trim();
                var finalName = line.substring(separatorIndex + SEPARATOR.length()).trim();
                if (wikiName.isEmpty() || finalName.isEmpty()) {
                    throw new IllegalArgumentException("Blank wiki mapping value at line " + lineNumber + ": " + line);
                }

                var renameResult = resolveRenameResult(finalNameIndex, currentArea, finalName, lineNumber);
                areaWikiEntryMap.get(currentArea).put(wikiName, WikiGameEntry.builder()
                        .area(currentArea)
                        .wikiName(wikiName)
                        .areaRenameResult(renameResult)
                        .build());
                actualCount++;
            }
            validateAreaCount(currentArea, expectedCount, actualCount, lines.size() + 1);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read wiki mapping file: " + wikiRomPath, e);
        }

        platformContext.setAreaWikiEntryMap(areaWikiEntryMap);
    }

    private Map<Area, Map<String, AreaRenameResult>> buildFinalNameIndex(PlatformContext platformContext) {
        var index = new LinkedHashMap<Area, Map<String, AreaRenameResult>>();
        platformContext.getAreaRenameResultMap().forEach((area, renameResults) -> {
            var areaIndex = index.computeIfAbsent(area, ignored -> new LinkedHashMap<>());
            renameResults.values().forEach(renameResult -> {
                var finalName = renameResult.getFinalName();
                if (finalName == null || finalName.isBlank()) {
                    return;
                }
                var previous = areaIndex.put(finalName, renameResult);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate finalName in areaRenameResultMap: " + area + " " + finalName);
                }
            });
        });
        return index;
    }

    private Area parseArea(String areaName, int lineNumber) {
        try {
            return Area.valueOf(areaName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown area at line " + lineNumber + ": " + areaName, e);
        }
    }

    private AreaRenameResult resolveRenameResult(Map<Area, Map<String, AreaRenameResult>> finalNameIndex,
                                                Area area,
                                                String finalName,
                                                int lineNumber) {
        if (UNMATCHED_MARKER.equals(finalName)) {
            return null;
        }

        var renameResult = finalNameIndex.getOrDefault(area, Map.of()).get(finalName);
        if (renameResult == null) {
            throw new IllegalStateException("No AreaRenameResult found for " + area + " finalName at line "
                    + lineNumber + ": " + finalName);
        }
        return renameResult;
    }

    private void validateAreaCount(Area area, int expectedCount, int actualCount, int lineNumber) {
        if (area != null && expectedCount != actualCount) {
            throw new IllegalStateException("Wiki mapping count mismatch before line " + lineNumber + ": "
                    + area + " expected " + expectedCount + " but found " + actualCount);
        }
    }
}

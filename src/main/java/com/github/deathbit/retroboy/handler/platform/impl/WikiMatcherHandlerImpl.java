package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.domain.AreaRenameResult;
import com.github.deathbit.retroboy.domain.AreaWikiMatchResult;
import com.github.deathbit.retroboy.domain.AreaWikiMismatch;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.domain.WikiGameEntry;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.WikiMatchType;
import com.github.deathbit.retroboy.handler.WikiMatcherHandler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class WikiMatcherHandlerImpl implements WikiMatcherHandler {

    private static final Pattern WIKI_SECTION_HEADER_PATTERN = Pattern.compile("^(JPN|USA|EUR)\\((\\d+)\\):$");

    private static final String WIKI_HAS_NO_MATCHING_ROM = "Wiki 中存在，但 areaPassMap 中没有匹配的原始 ROM 文件";
    private static final String ROM_HAS_NO_MATCHING_WIKI = "areaPassMap 中存在，但 Wiki 中没有匹配的游戏条目";
    private static final String WIKI_MATCHES_MULTIPLE_ROMS = "Wiki 条目匹配到多个 ROM 候选";
    private static final String ROM_MATCHES_MULTIPLE_WIKI_ENTRIES = "多个 Wiki 条目匹配到同一个原始 ROM 文件";
    private static final String WIKI_COUNT_MISMATCH = "Wiki header 数量和实际游戏条目数量不一致";
    private static final String DUPLICATE_WIKI_ENTRY = "Wiki 地区内存在重复游戏条目";
    private static final String FILE_CONTEXT_MISSING = "areaPassMap 中存在，但 fileContextMap 中缺少该原始 ROM 文件上下文";

    @Override
    public void handle(RuleContext ruleContext) {
        var parsedWiki = parseWiki(ruleContext);
        ruleContext.setAreaWikiEntryMap(parsedWiki.areaWikiEntryMap());
        ruleContext.setAreaWikiMatchResultMap(new LinkedHashMap<>());
        ruleContext.setAreaWikiMismatchMap(parsedWiki.areaWikiMismatchMap());

        for (var areaConfig : ruleContext.getPlatformPackTaskConfig().getAreaConfigs()) {
            var area = areaConfig.getArea();
            matchArea(ruleContext,
                    area,
                    ruleContext.getAreaWikiEntryMap().getOrDefault(area, Map.of()),
                    ruleContext.getAreaPassMap().getOrDefault(area, List.of()));
        }

        if (hasMismatch(ruleContext)) {
            throw new IllegalStateException("Wiki 和 areaPassMap 不一致，请检查 RuleContext.areaWikiMismatchMap");
        }
    }

    private ParsedWiki parseWiki(RuleContext ruleContext) {
        var wikiEntries = new LinkedHashMap<Area, Map<String, WikiGameEntry>>();
        var mismatches = new LinkedHashMap<Area, List<AreaWikiMismatch>>();
        var expectedCounts = new HashMap<Area, Integer>();
        var actualCounts = new HashMap<Area, Integer>();
        Area currentArea = null;

        try (var reader = openWikiReader(resolveWikiResourcePath(ruleContext))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                var trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                var headerMatcher = WIKI_SECTION_HEADER_PATTERN.matcher(trimmedLine);
                if (headerMatcher.matches()) {
                    currentArea = Area.valueOf(headerMatcher.group(1));
                    expectedCounts.put(currentArea, Integer.parseInt(headerMatcher.group(2)));
                    wikiEntries.computeIfAbsent(currentArea, ignored -> new LinkedHashMap<>());
                    actualCounts.putIfAbsent(currentArea, 0);
                    continue;
                }

                if (currentArea == null) {
                    continue;
                }

                actualCounts.compute(currentArea, (ignored, count) -> count == null ? 1 : count + 1);
                var areaEntries = wikiEntries.computeIfAbsent(currentArea, ignored -> new LinkedHashMap<>());
                if (areaEntries.containsKey(trimmedLine)) {
                    addMismatch(mismatches, AreaWikiMismatch.builder()
                            .area(currentArea)
                            .wikiName(trimmedLine)
                            .reason(DUPLICATE_WIKI_ENTRY)
                            .wikiLineNumber(lineNumber)
                            .build());
                    continue;
                }
                areaEntries.put(trimmedLine, WikiGameEntry.builder()
                        .area(currentArea)
                        .wikiName(trimmedLine)
                        .lineNumber(lineNumber)
                        .build());
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 Wiki 文件失败", e);
        }

        expectedCounts.forEach((area, expectedCount) -> {
            var actualCount = actualCounts.getOrDefault(area, 0);
            if (expectedCount != actualCount) {
                addMismatch(mismatches, AreaWikiMismatch.builder()
                        .area(area)
                        .reason(String.format("%s: expected=%d, actual=%d", WIKI_COUNT_MISMATCH, expectedCount, actualCount))
                        .build());
            }
        });

        return new ParsedWiki(wikiEntries, mismatches);
    }

    private BufferedReader openWikiReader(String wikiResourcePath) throws IOException {
        var resource = new ClassPathResource(wikiResourcePath);
        if (resource.exists()) {
            return new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        }

        var sourceResourcePath = Path.of("src", "main", "resources", wikiResourcePath);
        if (Files.exists(sourceResourcePath)) {
            return Files.newBufferedReader(sourceResourcePath, StandardCharsets.UTF_8);
        }

        throw new IOException("Wiki resource not found: " + wikiResourcePath);
    }

    private String resolveWikiResourcePath(RuleContext ruleContext) {
        var wiki = ruleContext.getPlatformPackTaskConfig().getWiki();
        var wikiName = ruleContext.getPlatform().name();
        if (wiki != null && !wiki.isBlank() && !wiki.startsWith("http://") && !wiki.startsWith("https://")) {
            wikiName = wiki.substring(wiki.lastIndexOf('/') + 1).replaceFirst("\\.txt$", "");
        }
        return String.format("wiki/new/%s.txt", wikiName);
    }

    private void matchArea(RuleContext ruleContext, Area area, Map<String, WikiGameEntry> wikiEntries, List<String> passFiles) {
        var areaMatches = ruleContext.getAreaWikiMatchResultMap().computeIfAbsent(area, ignored -> new LinkedHashMap<>());
        var areaMismatches = ruleContext.getAreaWikiMismatchMap().computeIfAbsent(area, ignored -> new ArrayList<>());
        var candidates = buildRomCandidates(ruleContext, area, passFiles, areaMismatches);
        var matchedFileNames = new HashSet<String>();
        var matchedWikiNames = new HashSet<String>();

        for (var wikiEntry : wikiEntries.values()) {
            var matchCandidate = findMatch(wikiEntry.getWikiName(), candidates);
            if (matchCandidate.candidates().isEmpty()) {
                continue;
            }
            if (matchCandidate.candidates().size() > 1) {
                areaMismatches.add(AreaWikiMismatch.builder()
                        .area(area)
                        .wikiName(wikiEntry.getWikiName())
                        .reason(WIKI_MATCHES_MULTIPLE_ROMS)
                        .wikiLineNumber(wikiEntry.getLineNumber())
                        .build());
                continue;
            }

            var romCandidate = matchCandidate.candidates().get(0);
            if (matchedFileNames.contains(romCandidate.originalFileName())) {
                areaMismatches.add(AreaWikiMismatch.builder()
                        .area(area)
                        .wikiName(wikiEntry.getWikiName())
                        .originalFileName(romCandidate.originalFileName())
                        .renamedFileName(romCandidate.renamedFileName())
                        .reason(ROM_MATCHES_MULTIPLE_WIKI_ENTRIES)
                        .wikiLineNumber(wikiEntry.getLineNumber())
                        .build());
                continue;
            }

            areaMatches.put(wikiEntry.getWikiName(), AreaWikiMatchResult.builder()
                    .wikiName(wikiEntry.getWikiName())
                    .originalFileName(romCandidate.originalFileName())
                    .renamedFileName(romCandidate.renamedFileName())
                    .matchType(matchCandidate.matchType())
                    .wikiLineNumber(wikiEntry.getLineNumber())
                    .build());
            matchedWikiNames.add(wikiEntry.getWikiName());
            matchedFileNames.add(romCandidate.originalFileName());
        }

        wikiEntries.values().stream()
                .filter(wikiEntry -> !matchedWikiNames.contains(wikiEntry.getWikiName()))
                .filter(wikiEntry -> areaMismatches.stream().noneMatch(mismatch -> wikiEntry.getWikiName().equals(mismatch.getWikiName())))
                .forEach(wikiEntry -> areaMismatches.add(AreaWikiMismatch.builder()
                        .area(area)
                        .wikiName(wikiEntry.getWikiName())
                        .reason(WIKI_HAS_NO_MATCHING_ROM)
                        .wikiLineNumber(wikiEntry.getLineNumber())
                        .build()));

        candidates.stream()
                .filter(candidate -> !matchedFileNames.contains(candidate.originalFileName()))
                .forEach(candidate -> areaMismatches.add(AreaWikiMismatch.builder()
                        .area(area)
                        .originalFileName(candidate.originalFileName())
                        .renamedFileName(candidate.renamedFileName())
                        .reason(ROM_HAS_NO_MATCHING_WIKI)
                        .build()));
    }

    private List<RomCandidate> buildRomCandidates(RuleContext ruleContext, Area area, List<String> passFiles, List<AreaWikiMismatch> areaMismatches) {
        var candidates = new ArrayList<RomCandidate>();
        for (var originalFileName : passFiles) {
            var fileContext = ruleContext.getFileContextMap().get(originalFileName);
            if (fileContext == null) {
                areaMismatches.add(AreaWikiMismatch.builder()
                        .area(area)
                        .originalFileName(originalFileName)
                        .renamedFileName(resolveRenamedFileName(ruleContext, area, originalFileName))
                        .reason(FILE_CONTEXT_MISSING)
                        .build());
                continue;
            }
            var romName = resolveRomName(fileContext);
            candidates.add(new RomCandidate(originalFileName,
                    resolveRenamedFileName(ruleContext, area, originalFileName),
                    romName,
                    normalizeForWikiMatch(romName),
                    looseNormalizeForWikiMatch(romName)));
        }
        return candidates;
    }

    private MatchCandidate findMatch(String wikiName, List<RomCandidate> candidates) {
        var exactCandidates = candidates.stream().filter(candidate -> wikiName.equals(candidate.romName())).toList();
        if (!exactCandidates.isEmpty()) {
            return new MatchCandidate(WikiMatchType.EXACT, exactCandidates);
        }

        var normalizedWikiName = normalizeForWikiMatch(wikiName);
        var normalizedCandidates = candidates.stream().filter(candidate -> normalizedWikiName.equals(candidate.normalizedRomName())).toList();
        if (!normalizedCandidates.isEmpty()) {
            return new MatchCandidate(WikiMatchType.NORMALIZED, normalizedCandidates);
        }

        var looseNormalizedWikiName = looseNormalizeForWikiMatch(wikiName);
        return new MatchCandidate(WikiMatchType.LOOSE_NORMALIZED,
                candidates.stream().filter(candidate -> looseNormalizedWikiName.equals(candidate.looseNormalizedRomName())).toList());
    }

    private String resolveRomName(FileContext fileContext) {
        if (fileContext.getNamePart() != null && !fileContext.getNamePart().isBlank()) {
            return fileContext.getNamePart();
        }
        if (fileContext.getFullName() != null && !fileContext.getFullName().isBlank()) {
            return fileContext.getFullName();
        }
        return fileContext.getFileName();
    }

    private String resolveRenamedFileName(RuleContext ruleContext, Area area, String originalFileName) {
        return ruleContext.getAreaRenameResultMap().getOrDefault(area, Map.of())
                .getOrDefault(originalFileName, AreaRenameResult.builder().newName(originalFileName).build())
                .getNewName();
    }

    private String normalizeForWikiMatch(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('“', '"')
                .replace('”', '"')
                .replace('–', '-')
                .replace('—', '-')
                .replace("&", "and")
                .replaceAll("[^\\p{Alnum}]+", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String looseNormalizeForWikiMatch(String value) {
        return normalizeForWikiMatch(value)
                .replace("ou", "o")
                .replace("uu", "u");
    }

    private void addMismatch(Map<Area, List<AreaWikiMismatch>> mismatches, AreaWikiMismatch mismatch) {
        mismatches.computeIfAbsent(mismatch.getArea(), ignored -> new ArrayList<>()).add(mismatch);
    }

    private boolean hasMismatch(RuleContext ruleContext) {
        return ruleContext.getAreaWikiMismatchMap().values().stream().anyMatch(mismatches -> !mismatches.isEmpty());
    }

    private record ParsedWiki(Map<Area, Map<String, WikiGameEntry>> areaWikiEntryMap,
                              Map<Area, List<AreaWikiMismatch>> areaWikiMismatchMap) {
    }

    private record RomCandidate(String originalFileName,
                                String renamedFileName,
                                String romName,
                                String normalizedRomName,
                                String looseNormalizedRomName) {
    }

    private record MatchCandidate(WikiMatchType matchType, List<RomCandidate> candidates) {
    }
}

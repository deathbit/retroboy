package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHandler implements Handler {

    private static final List<MediaType> MEDIA_TYPES = List.of(
            imageMediaType("3dboxes"),
            imageMediaType("backcovers"),
            imageMediaType("covers"),
            imageMediaType("fanart"),
            new MediaType("manuals", ".pdf", null),
            imageMediaType("marquees"),
            imageMediaType("miximages"),
            imageMediaType("physicalmedia"),
            imageMediaType("screenshots"),
            imageMediaType("titlescreens"),
            new MediaType("videos", ".mp4", null)
    );

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle() throws Exception {
        var ruleContext = prepareRuleContext();
        initializeRuleState(ruleContext);
        selectAreaFiles(ruleContext);
        cleanTargetDirectory(ruleContext);
        createAreaDirectories(ruleContext);
        copyAreaFiles(ruleContext);
        renameAreaFiles(ruleContext);
        checkMissingMediaFiles(ruleContext);
        writeProcessingReports(ruleContext);
    }

    private void initializeRuleState(RuleContext ruleContext) {
        ruleContext.setRuleMap(getRuleMap());
        ruleContext.setAreaRenameReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaDuplicateNameReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaFailureReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaMissingMediaReportMap(createEmptyAreaMissingMediaReportMap(ruleContext));
    }

    private void selectAreaFiles(RuleContext ruleContext) {
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var area = areaConfig.getArea();
            ruleContext.setCurrentArea(area);
            ruleContext.setCurrentAreaConfig(ruleContext.getRuleConfig().getTargetAreaConfigs().stream().filter(targetAreaConfig -> area == targetAreaConfig.getArea()).findFirst().orElse(null));
            var rule = ruleContext.getRuleMap().get(area);

            for (var entry : ruleContext.getFileContextMap().entrySet()) {
                var fileName = entry.getKey();
                var fileContext = entry.getValue();
                ruleContext.setFailureReasons(new ArrayList<>());
                if (rule.pass(ruleContext, fileContext)) {
                    ruleContext.getAreaFinalMap().get(area).add(fileName);
                } else {
                    ruleContext.getAreaFailureReportMap().get(area).add(failureReportLine(fileName, String.join(", ", ruleContext.getFailureReasons())));
                }
            }
        }
        ruleContext.setCurrentArea(null);
    }

    private String failureReportLine(String fileName, String failureReason) {
        return fileName + " - " + failureReason;
    }

    private void cleanTargetDirectory(RuleContext ruleContext) throws Exception {
        fileComponent.batchCleanDirs(List.of(ruleContext.getRuleConfig().getTargetDirBase()));
    }

    private void createAreaDirectories(RuleContext ruleContext) throws Exception {
        ruleContext.setDirsToCreate(ruleContext.getAreaFinalMap().keySet().stream()
                .map(areaKey -> Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), areaKey.name()).toString())
                .toList());
        fileComponent.batchCreateDirs(ruleContext.getDirsToCreate());
    }

    private void copyAreaFiles(RuleContext ruleContext) throws Exception {
        var filesToCopy = new ArrayList<CopyFileInput>();
        for (var entry : ruleContext.getAreaFinalMap().entrySet()) {
            for (var fileName : entry.getValue()) {
                filesToCopy.add(CopyFileInput.builder()
                        .srcFile(Paths.get(ruleContext.getRuleConfig().getRomDir(), fileName).toString())
                        .destDir(Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), entry.getKey().name()).toString())
                        .build());
            }
        }
        ruleContext.setFilesToCopy(filesToCopy);
        fileComponent.batchCopyFiles(ruleContext.getFilesToCopy());
    }

    private void renameAreaFiles(RuleContext ruleContext) throws Exception {
        ruleContext.setFilesToRename(buildRenameFileInputs(ruleContext));
        if (!ruleContext.getFilesToRename().isEmpty()) {
            fileComponent.batchRenameFiles(ruleContext.getFilesToRename());
        }
    }

    private Map<Area, List<String>> createEmptyAreaReportMap(RuleContext ruleContext) {
        var areaReportMap = new LinkedHashMap<Area, List<String>>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            areaReportMap.put(areaConfig.getArea(), new ArrayList<>());
        }

        return areaReportMap;
    }

    private Map<Area, Map<String, List<String>>> createEmptyAreaMissingMediaReportMap(RuleContext ruleContext) {
        var areaReportMap = new LinkedHashMap<Area, Map<String, List<String>>>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var mediaReportMap = new LinkedHashMap<String, List<String>>();
            for (var mediaType : MEDIA_TYPES) {
                mediaReportMap.put(mediaType.name(), new ArrayList<>());
            }
            areaReportMap.put(areaConfig.getArea(), mediaReportMap);
        }

        return areaReportMap;
    }

    private List<RenameFileInput> buildRenameFileInputs(RuleContext ruleContext) {
        var filesToRename = new ArrayList<RenameFileInput>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            filesToRename.addAll(buildAreaRenameFileInputs(
                    ruleContext,
                    areaConfig,
                    ruleContext.getAreaRenameReportMap().get(areaConfig.getArea()),
                    ruleContext.getAreaDuplicateNameReportMap().get(areaConfig.getArea())));
        }

        return filesToRename;
    }

    private List<RenameFileInput> buildAreaRenameFileInputs(RuleContext ruleContext,
                                                            AreaConfig areaConfig,
                                                            List<String> renameReportLines,
                                                            List<String> duplicateNameReportLines) {
        var finalFiles = ruleContext.getAreaFinalMap().get(areaConfig.getArea());
        if (finalFiles == null || finalFiles.isEmpty()) {
            return List.of();
        }

        var renamePlanMap = new LinkedHashMap<String, List<RenamePlan>>();
        for (var fileName : finalFiles) {
            var fileContext = ruleContext.getFileContextMap().get(fileName);
            if (fileContext == null) {
                continue;
            }
            var renamePlan = buildRenamePlan(fileContext);
            renamePlanMap.computeIfAbsent(renamePlan.targetFileName(), ignored -> new ArrayList<>()).add(renamePlan);
        }

        var filesToRename = new ArrayList<RenameFileInput>();
        for (var renamePlans : renamePlanMap.values()) {
            if (renamePlans.size() == 1) {
                addRenameFileInput(ruleContext, areaConfig.getArea(), renamePlans.get(0), renamePlans.get(0).targetFileName(), renameReportLines, false, filesToRename);
                continue;
            }

            addDuplicateNameReport(renamePlans, duplicateNameReportLines, areaConfig.getRenameOptions());
            if (areaConfig.getRenameOptions() == null || areaConfig.getRenameOptions().isEmpty()) {
                continue;
            }

            for (var renamePlan : renamePlans) {
                var renameOption = findRenameOption(areaConfig.getRenameOptions(), renamePlan.fileContext().getFileName());
                if (renameOption != null && !renameOption.isBlank()) {
                    addRenameFileInput(ruleContext, areaConfig.getArea(), renamePlan, withOriginalExtension(renameOption, renamePlan.fileContext().getFileName()), renameReportLines, true, filesToRename);
                }
            }
        }

        return filesToRename;
    }

    private String findRenameOption(List<AreaConfig.RenameOption> renameOptions, String fileName) {
        if (renameOptions == null || renameOptions.isEmpty()) {
            return null;
        }

        return renameOptions.stream()
                .filter(renameOption -> fileName.equals(renameOption.getOldName()))
                .map(AreaConfig.RenameOption::getNewName)
                .findFirst()
                .orElse(null);
    }

    private void addDuplicateNameReport(List<RenamePlan> renamePlans, List<String> duplicateNameReportLines, List<AreaConfig.RenameOption> renameOptions) {
        var targetFileName = renamePlans.get(0).targetFileName();
        var duplicateFileNames = renamePlans.stream()
                .map(renamePlan -> renamePlan.fileContext().getFileName())
                .collect(Collectors.joining(", "));
        var action = renameOptions == null || renameOptions.isEmpty()
                ? "地区未配置重命名选项，保持原文件名"
                : "使用地区重命名选项处理";
        duplicateNameReportLines.add(targetFileName + " - DUPLICATE_NAME: 去除标签后文件名一致，文件: " + duplicateFileNames + "，" + action);
    }

    private RenamePlan buildRenamePlan(FileContext fileContext) {
        var normalizedNamePart = normalizeLeadingArticle(fileContext.getNamePart());
        var targetFileName = normalizedNamePart + extension(fileContext.getFileName());
        return new RenamePlan(fileContext, targetFileName, !normalizedNamePart.equals(fileContext.getNamePart()));
    }

    private void addRenameFileInput(RuleContext ruleContext,
                                    Area area,
                                    RenamePlan renamePlan,
                                    String targetFileName,
                                    List<String> renameReportLines,
                                    boolean renameByOption,
                                    List<RenameFileInput> filesToRename) {
        var fileContext = renamePlan.fileContext();
        if (fileContext.getFileName().equals(targetFileName)) {
            return;
        }

        filesToRename.add(RenameFileInput.builder()
                .srcFile(Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), area.name(), fileContext.getFileName()))
                .newName(targetFileName)
                .build());

        if (renameByOption) {
            renameReportLines.add(fileContext.getFileName() + " -> " + targetFileName + " - RENAME_OPTION: 去除标签后文件名重复，使用地区重命名配置");
        } else if (renamePlan.articleAdjusted()) {
            renameReportLines.add(fileContext.getFileName() + " -> " + targetFileName + " - ARTICLE_RENAME: 调整前置冠词");
        }
    }

    private String normalizeLeadingArticle(String namePart) {
        return normalizeTrailingArticle(normalizeTrailingArticle(namePart, "The"), "A");
    }

    private String normalizeTrailingArticle(String namePart, String article) {
        var suffix = ", " + article;
        var separatorIndex = namePart.indexOf(" - ");
        if (separatorIndex == -1) {
            if (namePart.endsWith(suffix)) {
                return article + " " + namePart.substring(0, namePart.length() - suffix.length());
            }
            return namePart;
        }

        var title = namePart.substring(0, separatorIndex);
        if (!title.endsWith(suffix)) {
            return namePart;
        }

        return article + " " + title.substring(0, title.length() - suffix.length()) + namePart.substring(separatorIndex);
    }

    private String withOriginalExtension(String fileName, String originalFileName) {
        var originalExtension = extension(originalFileName);
        if (originalExtension.isEmpty() || fileName.endsWith(originalExtension)) {
            return fileName;
        }

        return fileName + originalExtension;
    }

    private String extension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }

        return fileName.substring(dotIndex);
    }

    private void checkMissingMediaFiles(RuleContext ruleContext) throws Exception {
        var downloadedMediaDirBase = appConfig.getGlobalConfig().getDownloadedMediaDirBase();
        if (downloadedMediaDirBase == null || downloadedMediaDirBase.isBlank()) {
            throw new IllegalStateException("app.config.globalConfig.downloadedMediaDirBase must be configured");
        }

        var platformMediaDirName = Paths.get(ruleContext.getRuleConfig().getTargetDirBase()).getFileName().toString();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var area = areaConfig.getArea();
            var areaTargetDir = Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), area.name());
            try (var files = Files.list(areaTargetDir)) {
                var gameNames = files
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .sorted()
                        .map(this::withoutExtension)
                        .toList();
                addMissingMediaReportLines(ruleContext, downloadedMediaDirBase, platformMediaDirName, area, gameNames);
            }
        }
    }

    private void addMissingMediaReportLines(RuleContext ruleContext,
                                            String downloadedMediaDirBase,
                                            String platformMediaDirName,
                                            Area area,
                                            List<String> gameNames) {
        var missingMediaReportMap = ruleContext.getAreaMissingMediaReportMap().get(area);
        for (var gameName : gameNames) {
            for (var mediaType : MEDIA_TYPES) {
                var mediaFile = Paths.get(
                        downloadedMediaDirBase,
                        platformMediaDirName,
                        mediaType.name(),
                        area.name(),
                        gameName + mediaType.reportExtension());
                if (!mediaFileExists(mediaFile, mediaType.fallbackExtension())) {
                    missingMediaReportMap.get(mediaType.name()).add(mediaFile.toString());
                }
            }
        }
    }

    private boolean mediaFileExists(Path mediaFile, String fallbackExtension) {
        if (Files.isRegularFile(mediaFile)) {
            return true;
        }
        if (fallbackExtension == null) {
            return false;
        }

        return Files.isRegularFile(withExtension(mediaFile, fallbackExtension));
    }

    private Path withExtension(Path path, String extension) {
        var fileName = path.getFileName().toString();
        return path.resolveSibling(withoutExtension(fileName) + extension);
    }

    private String withoutExtension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    private void writeProcessingReports(RuleContext ruleContext) throws Exception {
        var reportDir = Paths.get("report");
        Files.createDirectories(reportDir);
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var area = areaConfig.getArea();
            var lines = buildProcessingReportYamlLines(
                    ruleContext,
                    area,
                    ruleContext.getAreaRenameReportMap().get(area),
                    ruleContext.getAreaDuplicateNameReportMap().get(area),
                    ruleContext.getAreaMissingMediaReportMap().get(area),
                    ruleContext.getAreaFailureReportMap().get(area));
            var reportFile = reportDir.resolve(ruleContext.getPlatform().name() + "-" + area.name() + ".yaml");
            Files.write(reportFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private List<String> buildProcessingReportYamlLines(RuleContext ruleContext,
                                                        Area area,
                                                        List<String> renameReportLines,
                                                        List<String> duplicateNameReportLines,
                                                        Map<String, List<String>> missingMediaReportLines,
                                                        List<String> failureReportLines) {
        var finalFiles = ruleContext.getAreaFinalMap().get(area);
        var passedCount = finalFiles.size();
        var lines = new ArrayList<String>();
        lines.add("platform: " + yamlValue(ruleContext.getPlatform().name()));
        lines.add("area: " + yamlValue(area.name()));
        lines.add("romDir: " + yamlValue(ruleContext.getRuleConfig().getRomDir()));
        lines.add("summary:");
        lines.add("  total: " + ruleContext.getFileContextMap().size());
        lines.add("  passed: " + passedCount);
        lines.add("  failed: " + (ruleContext.getFileContextMap().size() - passedCount));

        var passedFiles = new ArrayList<String>();
        for (var fileName : ruleContext.getFileContextMap().keySet()) {
            if (finalFiles.contains(fileName)) {
                passedFiles.add(fileName);
            }
        }
        addYamlList(lines, "passed", passedFiles, "");
        addYamlList(lines, "renamed", renameReportLines, "");
        addYamlList(lines, "duplicateNames", duplicateNameReportLines, "");
        addMissingMediaYaml(lines, missingMediaReportLines);

        var failedFiles = new ArrayList<String>();
        if (!failureReportLines.isEmpty()) {
            failedFiles.addAll(failureReportLines);
        } else {
            for (var fileName : ruleContext.getFileContextMap().keySet()) {
                if (!finalFiles.contains(fileName)) {
                    failedFiles.add(fileName);
                }
            }
        }
        addYamlList(lines, "failed", failedFiles, "");

        return lines;
    }

    private void addMissingMediaYaml(List<String> lines, Map<String, List<String>> missingMediaReportLines) {
        if (!hasMissingMediaReportLines(missingMediaReportLines)) {
            lines.add("missingMedia: {}");
            return;
        }

        lines.add("missingMedia:");
        for (var mediaType : MEDIA_TYPES) {
            var missingMediaFiles = missingMediaReportLines.get(mediaType.name());
            if (missingMediaFiles == null || missingMediaFiles.isEmpty()) {
                continue;
            }
            lines.add("  " + yamlValue(mediaType.name()) + ":");
            for (var missingMediaFile : missingMediaFiles) {
                lines.add("    - " + yamlValue(missingMediaFile));
            }
        }
    }

    private void addYamlList(List<String> lines, String key, List<String> values, String indent) {
        if (values == null || values.isEmpty()) {
            lines.add(indent + key + ": []");
            return;
        }

        lines.add(indent + key + ":");
        for (var value : values) {
            lines.add(indent + "  - " + yamlValue(value));
        }
    }

    private String yamlValue(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                + "\"";
    }

    private boolean hasMissingMediaReportLines(Map<String, List<String>> missingMediaReportLines) {
        return missingMediaReportLines != null
                && missingMediaReportLines.values().stream().anyMatch(lines -> lines != null && !lines.isEmpty());
    }

    private RuleContext prepareRuleContext() throws Exception {
        var ruleContext = initializeRuleContext();
        ruleContext.setLicensed(parseLicensedGames(ruleContext.getRuleConfig().getDatFile()));
        populateFileContextMap(ruleContext, ruleContext.getRuleConfig().getRomDir());

        return ruleContext;
    }

    private RuleContext initializeRuleContext() {
        var ruleContext = new RuleContext();
        ruleContext.setPlatform(getPlatform());
        ruleContext.setRuleConfig(appConfig.getRuleConfigMap().get(ruleContext.getPlatform()));
        ruleContext.setFileContextMap(new LinkedHashMap<>());
        ruleContext.setAreaFinalMap(new LinkedHashMap<>());
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            ruleContext.getAreaFinalMap().put(areaConfig.getArea(), new LinkedHashSet<>());
        }
        ruleContext.setGlobalTagBlackList(appConfig.getGlobalConfig().getGlobalTagBlacklist());

        return ruleContext;
    }

    private Set<String> parseLicensedGames(String datFilePath) throws Exception {
        var licensed = new HashSet<String>();
        var document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(datFilePath));
        var gameNodes = document.getElementsByTagName("game");
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var name = ((Element) gameNodes.item(i)).getAttribute("name");
            if (!name.isEmpty()) {
                licensed.add(name);
            }
        }

        return licensed;
    }

    private void populateFileContextMap(RuleContext ruleContext, String romDirPath) throws Exception {
        var files = new File(romDirPath).listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (var file : files) {
                ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
            }
        }
    }

    private FileContext buildFileContext(String fileName) throws Exception {
        var fullName = fileName;
        if (fileName.contains(".")) {
            fullName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        var namePart = fullName;
        var tagPart = "";
        var tags = new HashSet<String>();

        var firstParen = fullName.indexOf('(');
        if (firstParen != -1) {
            namePart = fullName.substring(0, firstParen).trim();
            tagPart = fullName.substring(firstParen);
            int start = 0;
            while ((start = tagPart.indexOf('(', start)) != -1) {
                int end = tagPart.indexOf(')', start);
                if (end != -1) {
                    tags.add(tagPart.substring(start + 1, end));
                    start = end + 1;
                } else {
                    break;
                }
            }
        }

        return FileContext.builder()
                .fileName(fileName)
                .fullName(fullName)
                .namePart(namePart)
                .tagPart(tagPart)
                .tags(tags)
                .build();
    }

    @Override
    public Map<Area, Rule> getRuleMap() {
        return Map.of(
                Area.JPN, Rules.IS_JAPAN_BASE,
                Area.USA, Rules.IS_USA_BASE,
                Area.EUR, Rules.IS_EUROPE_BASE
        );
    }

    public abstract Platform getPlatform();

    private record RenamePlan(FileContext fileContext, String targetFileName, boolean articleAdjusted) {
    }

    private static MediaType imageMediaType(String name) {
        return new MediaType(name, ".png", ".jpg");
    }

    private record MediaType(String name, String reportExtension, String fallbackExtension) {
    }
}

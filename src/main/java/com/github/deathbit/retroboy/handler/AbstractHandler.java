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
import com.github.deathbit.retroboy.rule.RuleResult;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractHandler implements Handler {

    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle() throws Exception {
        var ruleContext = prepareRuleContext();
        initializeRuleState(ruleContext);
        selectAreaFiles(ruleContext);
        preferEuropeVersionForEuropeArea(ruleContext);
        cleanTargetDirectory(ruleContext);
        createAreaDirectories(ruleContext);
        copyAreaFiles(ruleContext);
        renameAreaFiles(ruleContext);
        writeProcessingReports(ruleContext);
    }

    private void initializeRuleState(RuleContext ruleContext) {
        ruleContext.setRuleMap(getRuleMap());
        ruleContext.setAreaRuleResultMap(initializeAreaRuleResultMap(ruleContext));
        ruleContext.setAreaRenameReportMap(initializeAreaReportMap(ruleContext));
        ruleContext.setAreaDuplicateNameReportMap(initializeAreaReportMap(ruleContext));
    }

    private void selectAreaFiles(RuleContext ruleContext) {
        for (var entry : ruleContext.getFileContextMap().entrySet()) {
            var fileName = entry.getKey();
            var fileContext = entry.getValue();

            for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
                var area = areaConfig.getArea();
                var rule = ruleContext.getRuleMap().get(area);
                var ruleResult = evaluateAreaRule(ruleContext, fileContext, areaConfig, rule);
                ruleContext.getAreaRuleResultMap().get(area).put(fileName, ruleResult);

                if (ruleResult.isPassed()) {
                    ruleContext.getAreaFinalMap().get(area).add(fileName);
                }
            }
        }
    }

    private void cleanTargetDirectory(RuleContext ruleContext) throws Exception {
        fileComponent.batchCleanDirs(List.of(ruleContext.getRuleConfig().getTargetDirBase()));
    }

    private void createAreaDirectories(RuleContext ruleContext) throws Exception {
        ruleContext.setDirsToCreate(ruleContext.getAreaFinalMap().keySet().stream()
                .map(area -> Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), area.name()).toString())
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

    private Map<Area, List<String>> initializeAreaReportMap(RuleContext ruleContext) {
        var areaReportMap = new LinkedHashMap<Area, List<String>>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            areaReportMap.put(areaConfig.getArea(), new ArrayList<>());
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

    private void preferEuropeVersionForEuropeArea(RuleContext ruleContext) {
        var europeFinalFiles = ruleContext.getAreaFinalMap().get(Area.EUR);
        var europeRuleResultMap = ruleContext.getAreaRuleResultMap().get(Area.EUR);
        if (europeFinalFiles == null || europeRuleResultMap == null || europeFinalFiles.isEmpty()) {
            return;
        }

        var passedFileContextMap = new LinkedHashMap<String, List<FileContext>>();
        for (var fileName : europeFinalFiles) {
            var fileContext = ruleContext.getFileContextMap().get(fileName);
            if (fileContext != null) {
                passedFileContextMap.computeIfAbsent(fileContext.getNamePart(), ignored -> new ArrayList<>()).add(fileContext);
            }
        }

        var filesToRemove = new ArrayList<String>();
        for (var fileContexts : passedFileContextMap.values()) {
            var europeVersions = fileContexts.stream()
                    .filter(this::isEuropeVersion)
                    .map(FileContext::getFileName)
                    .toList();
            if (europeVersions.isEmpty()) {
                continue;
            }

            for (var fileContext : fileContexts) {
                if (!isEuropeVersion(fileContext)) {
                    filesToRemove.add(fileContext.getFileName());
                    europeRuleResultMap.put(fileContext.getFileName(), RuleResult.fail(
                            "PREFER_EUROPE_VERSION",
                            "存在同名 Europe 版本，已排除地区版本，保留: " + String.join(", ", europeVersions)));
                }
            }
        }

        europeFinalFiles.removeAll(filesToRemove);
    }

    private boolean isEuropeVersion(FileContext fileContext) {
        return fileContext.getTags().contains("Europe");
    }

    private RuleResult evaluateAreaRule(RuleContext ruleContext, FileContext fileContext, AreaConfig areaConfig, Rule rule) {
        RuleResult ruleResult = rule.evaluate(ruleContext, fileContext);
        RuleResult areaFileNameBlackListResult = evaluateAreaFileNameBlackList(fileContext, areaConfig);
        if (ruleResult.isPassed() && areaFileNameBlackListResult.isPassed()) {
            return RuleResult.pass();
        }

        return RuleResult.fail(ruleResult, areaFileNameBlackListResult);
    }

    private RuleResult evaluateAreaFileNameBlackList(FileContext fileContext, AreaConfig areaConfig) {
        var fileNameBlackList = areaConfig.getFileNameBlackList();
        if (fileNameBlackList == null || !fileNameBlackList.contains(fileContext.getFileName())) {
            return RuleResult.pass();
        }

        return RuleResult.fail("IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST", "命中地区文件名黑名单: " + fileContext.getFileName());
    }

    private Map<Area, Map<String, RuleResult>> initializeAreaRuleResultMap(RuleContext ruleContext) {
        var areaRuleResultMap = new LinkedHashMap<Area, Map<String, RuleResult>>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var ruleResultMap = new LinkedHashMap<String, RuleResult>();
            for (var skippedEntry : ruleContext.getSkippedFileReasonMap().entrySet()) {
                ruleResultMap.put(skippedEntry.getKey(), RuleResult.fail("PREVIOUS_REVISION", skippedEntry.getValue()));
            }
            areaRuleResultMap.put(areaConfig.getArea(), ruleResultMap);
        }

        return areaRuleResultMap;
    }

    private void writeProcessingReports(RuleContext ruleContext) throws Exception {
        var reportDir = Paths.get("report");
        Files.createDirectories(reportDir);
        for (var entry : ruleContext.getAreaRuleResultMap().entrySet()) {
            var area = entry.getKey();
            var lines = buildProcessingReportLines(
                    ruleContext,
                    area,
                    entry.getValue(),
                    ruleContext.getAreaRenameReportMap().get(area),
                    ruleContext.getAreaDuplicateNameReportMap().get(area));
            var reportFile = reportDir.resolve(ruleContext.getPlatform().name() + "-" + area.name() + ".txt");
            Files.write(reportFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private List<String> buildProcessingReportLines(RuleContext ruleContext,
                                                    Area area,
                                                    Map<String, RuleResult> ruleResultMap,
                                                    List<String> renameReportLines,
                                                    List<String> duplicateNameReportLines) {
        var passedCount = ruleResultMap.values().stream().filter(RuleResult::isPassed).count();
        var lines = new ArrayList<String>();
        lines.add("平台: " + ruleContext.getPlatform().name());
        lines.add("地区: " + area.name());
        lines.add("ROM目录: " + ruleContext.getRuleConfig().getRomDir());
        lines.add("文件总数: " + ruleResultMap.size());
        lines.add("通过校验: " + passedCount);
        lines.add("未通过校验: " + (ruleResultMap.size() - passedCount));
        lines.add("");
        lines.add("通过:");
        for (var entry : ruleResultMap.entrySet()) {
            if (entry.getValue().isPassed()) {
                lines.add(entry.getKey());
            }
        }

        if (renameReportLines != null && !renameReportLines.isEmpty()) {
            lines.add("");
            lines.add("重命名:");
            lines.addAll(renameReportLines);
        }

        if (duplicateNameReportLines != null && !duplicateNameReportLines.isEmpty()) {
            lines.add("");
            lines.add("去除标签后同名:");
            lines.addAll(duplicateNameReportLines);
        }

        lines.add("");
        lines.add("未通过:");
        for (var entry : ruleResultMap.entrySet()) {
            var ruleResult = entry.getValue();
            if (!ruleResult.isPassed()) {
                lines.add(entry.getKey() + " - " + String.join("; ", ruleResult.getFailures()));
            }
        }

        return lines;
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
        ruleContext.setSkippedFileReasonMap(new LinkedHashMap<>());
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
            for (var file : files) {
                var previousRevision = previousRevision(file.getName());
                if (previousRevision != null) {
                    var removedFileContext = ruleContext.getFileContextMap().remove(previousRevision);
                    if (removedFileContext != null) {
                        ruleContext.getSkippedFileReasonMap().put(previousRevision, "存在新版修订，已被替代: " + file.getName());
                    }
                }
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

    private String previousRevision(String filename) {
        var matcher = REV_TAG.matcher(filename);
        if (!matcher.find()) {
            return null;
        }

        try {
            var revision = Integer.parseInt(matcher.group(1));
            if (revision == 1) {
                return filename.substring(0, matcher.start())
                        .concat(filename.substring(matcher.end()))
                        .replaceAll("\\s+\\.", ".")
                        .replaceAll("\\s{2,}", " ")
                        .trim();
            }
            return filename.substring(0, matcher.start())
                    .concat("(Rev " + (revision - 1) + ")")
                    .concat(filename.substring(matcher.end()));
        } catch (NumberFormatException e) {
            return null;
        }
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
}

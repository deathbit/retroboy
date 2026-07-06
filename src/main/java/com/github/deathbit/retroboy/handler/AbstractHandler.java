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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
import java.util.regex.Matcher;
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
        RuleContext ruleContext = buildRuleContext();
        Map<Area, Rule> ruleMap = getRuleMap();
        Map<Area, Map<String, RuleResult>> areaRuleResultMap = initializeAreaRuleResultMap(ruleContext);
        Map<Area, List<String>> areaRenameReportMap = initializeAreaRenameReportMap(ruleContext);
        Map<Area, List<String>> areaDuplicateNameReportMap = initializeAreaRenameReportMap(ruleContext);

        for (Map.Entry<String, FileContext> entry : ruleContext.getFileContextMap().entrySet()) {
            String fileName = entry.getKey();
            FileContext fileContext = entry.getValue();

            for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
                Area area = areaConfig.getArea();
                Rule rule = ruleMap.get(area);
                RuleResult ruleResult = evaluateAreaRule(ruleContext, fileContext, areaConfig, rule);
                areaRuleResultMap.get(area).put(fileName, ruleResult);

                if (ruleResult.isPassed()) {
                    ruleContext.getAreaFinalMap().get(area).add(fileName);
                }
            }
        }

        preferEuropeVersionForEuropeArea(ruleContext, areaRuleResultMap);
        fileComponent.batchCleanDirs(List.of(ruleContext.getRuleConfig().getTargetDirBase()));

        List<String> dirsToCreate = ruleContext.getAreaFinalMap().keySet().stream()
                .map(strings -> Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), strings.name()).toString())
                .collect(Collectors.toList());
        fileComponent.batchCreateDirs(dirsToCreate);

        List<CopyFileInput> filesToCopy = new ArrayList<>();
        for (Map.Entry<Area, Set<String>> entry : ruleContext.getAreaFinalMap().entrySet()) {
            for (String fileName : entry.getValue()) {
                filesToCopy.add(CopyFileInput.builder()
                        .srcFile(Paths.get(ruleContext.getRuleConfig().getRomDir(), fileName).toString())
                        .destDir(Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), entry.getKey().name()).toString())
                        .build());
            }
        }
        fileComponent.batchCopyFiles(filesToCopy);

        List<RenameFileInput> filesToRename = buildRenameFileInputs(ruleContext, areaRenameReportMap, areaDuplicateNameReportMap);
        if (!filesToRename.isEmpty()) {
            fileComponent.batchRenameFiles(filesToRename);
        }
        writeProcessingReports(ruleContext, areaRuleResultMap, areaRenameReportMap, areaDuplicateNameReportMap);
    }

    private Map<Area, List<String>> initializeAreaRenameReportMap(RuleContext ruleContext) {
        Map<Area, List<String>> areaRenameReportMap = new LinkedHashMap<>();
        for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            areaRenameReportMap.put(areaConfig.getArea(), new ArrayList<>());
        }

        return areaRenameReportMap;
    }

    private List<RenameFileInput> buildRenameFileInputs(RuleContext ruleContext,
                                                        Map<Area, List<String>> areaRenameReportMap,
                                                        Map<Area, List<String>> areaDuplicateNameReportMap) {
        List<RenameFileInput> filesToRename = new ArrayList<>();
        for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            filesToRename.addAll(buildAreaRenameFileInputs(
                    ruleContext,
                    areaConfig,
                    areaRenameReportMap.get(areaConfig.getArea()),
                    areaDuplicateNameReportMap.get(areaConfig.getArea())));
        }

        return filesToRename;
    }

    private List<RenameFileInput> buildAreaRenameFileInputs(RuleContext ruleContext,
                                                            AreaConfig areaConfig,
                                                            List<String> renameReportLines,
                                                            List<String> duplicateNameReportLines) {
        Set<String> finalFiles = ruleContext.getAreaFinalMap().get(areaConfig.getArea());
        if (finalFiles == null || finalFiles.isEmpty()) {
            return List.of();
        }

        Map<String, List<RenamePlan>> renamePlanMap = new LinkedHashMap<>();
        for (String fileName : finalFiles) {
            FileContext fileContext = ruleContext.getFileContextMap().get(fileName);
            if (fileContext == null) {
                continue;
            }
            RenamePlan renamePlan = buildRenamePlan(fileContext);
            renamePlanMap.computeIfAbsent(renamePlan.targetFileName(), ignored -> new ArrayList<>()).add(renamePlan);
        }

        List<RenameFileInput> filesToRename = new ArrayList<>();
        for (List<RenamePlan> renamePlans : renamePlanMap.values()) {
            if (renamePlans.size() == 1) {
                addRenameFileInput(ruleContext, areaConfig.getArea(), renamePlans.get(0), renamePlans.get(0).targetFileName(), renameReportLines, false, filesToRename);
                continue;
            }

            addDuplicateNameReport(renamePlans, duplicateNameReportLines, areaConfig.getRenameOptions());
            if (areaConfig.getRenameOptions() == null || areaConfig.getRenameOptions().isEmpty()) {
                continue;
            }

            for (RenamePlan renamePlan : renamePlans) {
                String renameOption = findRenameOption(areaConfig.getRenameOptions(), renamePlan.fileContext().getFileName());
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
        String targetFileName = renamePlans.get(0).targetFileName();
        String duplicateFileNames = renamePlans.stream()
                .map(renamePlan -> renamePlan.fileContext().getFileName())
                .collect(Collectors.joining(", "));
        String action = renameOptions == null || renameOptions.isEmpty()
                ? "地区未配置重命名选项，保持原文件名"
                : "使用地区重命名选项处理";
        duplicateNameReportLines.add(targetFileName + " - DUPLICATE_NAME: 去除标签后文件名一致，文件: " + duplicateFileNames + "，" + action);
    }

    private RenamePlan buildRenamePlan(FileContext fileContext) {
        String normalizedNamePart = normalizeLeadingArticle(fileContext.getNamePart());
        String targetFileName = normalizedNamePart + extension(fileContext.getFileName());
        return new RenamePlan(fileContext, targetFileName, !normalizedNamePart.equals(fileContext.getNamePart()));
    }

    private void addRenameFileInput(RuleContext ruleContext,
                                    Area area,
                                    RenamePlan renamePlan,
                                    String targetFileName,
                                    List<String> renameReportLines,
                                    boolean renameByOption,
                                    List<RenameFileInput> filesToRename) {
        FileContext fileContext = renamePlan.fileContext();
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
        String suffix = ", " + article;
        int separatorIndex = namePart.indexOf(" - ");
        if (separatorIndex == -1) {
            if (namePart.endsWith(suffix)) {
                return article + " " + namePart.substring(0, namePart.length() - suffix.length());
            }
            return namePart;
        }

        String title = namePart.substring(0, separatorIndex);
        if (!title.endsWith(suffix)) {
            return namePart;
        }

        return article + " " + title.substring(0, title.length() - suffix.length()) + namePart.substring(separatorIndex);
    }

    private String withOriginalExtension(String fileName, String originalFileName) {
        String originalExtension = extension(originalFileName);
        if (originalExtension.isEmpty() || fileName.endsWith(originalExtension)) {
            return fileName;
        }

        return fileName + originalExtension;
    }

    private String extension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }

        return fileName.substring(dotIndex);
    }

    private void preferEuropeVersionForEuropeArea(RuleContext ruleContext, Map<Area, Map<String, RuleResult>> areaRuleResultMap) {
        Set<String> europeFinalFiles = ruleContext.getAreaFinalMap().get(Area.EUR);
        Map<String, RuleResult> europeRuleResultMap = areaRuleResultMap.get(Area.EUR);
        if (europeFinalFiles == null || europeRuleResultMap == null || europeFinalFiles.isEmpty()) {
            return;
        }

        Map<String, List<FileContext>> passedFileContextMap = new LinkedHashMap<>();
        for (String fileName : europeFinalFiles) {
            FileContext fileContext = ruleContext.getFileContextMap().get(fileName);
            if (fileContext != null) {
                passedFileContextMap.computeIfAbsent(fileContext.getNamePart(), ignored -> new ArrayList<>()).add(fileContext);
            }
        }

        List<String> filesToRemove = new ArrayList<>();
        for (List<FileContext> fileContexts : passedFileContextMap.values()) {
            List<String> europeVersions = fileContexts.stream()
                    .filter(this::isEuropeVersion)
                    .map(FileContext::getFileName)
                    .collect(Collectors.toList());
            if (europeVersions.isEmpty()) {
                continue;
            }

            for (FileContext fileContext : fileContexts) {
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
        Set<String> fileNameBlackList = areaConfig.getFileNameBlackList();
        if (fileNameBlackList == null || !fileNameBlackList.contains(fileContext.getFileName())) {
            return RuleResult.pass();
        }

        return RuleResult.fail("IS_NOT_HIT_AREA_FILE_NAME_BLACKLIST", "命中地区文件名黑名单: " + fileContext.getFileName());
    }

    private Map<Area, Map<String, RuleResult>> initializeAreaRuleResultMap(RuleContext ruleContext) {
        Map<Area, Map<String, RuleResult>> areaRuleResultMap = new LinkedHashMap<>();
        for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            Map<String, RuleResult> ruleResultMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> skippedEntry : ruleContext.getSkippedFileReasonMap().entrySet()) {
                ruleResultMap.put(skippedEntry.getKey(), RuleResult.fail("PREVIOUS_REVISION", skippedEntry.getValue()));
            }
            areaRuleResultMap.put(areaConfig.getArea(), ruleResultMap);
        }

        return areaRuleResultMap;
    }

    private void writeProcessingReports(RuleContext ruleContext,
                                        Map<Area, Map<String, RuleResult>> areaRuleResultMap,
                                        Map<Area, List<String>> areaRenameReportMap,
                                        Map<Area, List<String>> areaDuplicateNameReportMap) throws Exception {
        Path reportDir = Paths.get("report");
        Files.createDirectories(reportDir);
        for (Map.Entry<Area, Map<String, RuleResult>> entry : areaRuleResultMap.entrySet()) {
            Area area = entry.getKey();
            List<String> lines = buildProcessingReportLines(
                    ruleContext,
                    area,
                    entry.getValue(),
                    areaRenameReportMap.get(area),
                    areaDuplicateNameReportMap.get(area));
            Path reportFile = reportDir.resolve(ruleContext.getPlatform().name() + "-" + area.name() + ".txt");
            Files.write(reportFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private List<String> buildProcessingReportLines(RuleContext ruleContext,
                                                    Area area,
                                                    Map<String, RuleResult> ruleResultMap,
                                                    List<String> renameReportLines,
                                                    List<String> duplicateNameReportLines) {
        long passedCount = ruleResultMap.values().stream().filter(RuleResult::isPassed).count();
        List<String> lines = new ArrayList<>();
        lines.add("平台: " + ruleContext.getPlatform().name());
        lines.add("地区: " + area.name());
        lines.add("ROM目录: " + ruleContext.getRuleConfig().getRomDir());
        lines.add("文件总数: " + ruleResultMap.size());
        lines.add("通过校验: " + passedCount);
        lines.add("未通过校验: " + (ruleResultMap.size() - passedCount));
        lines.add("");
        lines.add("通过:");
        for (Map.Entry<String, RuleResult> entry : ruleResultMap.entrySet()) {
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
        for (Map.Entry<String, RuleResult> entry : ruleResultMap.entrySet()) {
            RuleResult ruleResult = entry.getValue();
            if (!ruleResult.isPassed()) {
                lines.add(entry.getKey() + " - " + String.join("; ", ruleResult.getFailures()));
            }
        }

        return lines;
    }

    private RuleContext buildRuleContext() throws Exception {
        RuleContext ruleContext = initializeRuleContext();
        ruleContext.setLicensed(parseLicensedGames(ruleContext.getRuleConfig().getDatFile()));
        populateFileContextMap(ruleContext, ruleContext.getRuleConfig().getRomDir());

        return ruleContext;
    }

    private RuleContext initializeRuleContext() {
        RuleContext ruleContext = new RuleContext();
        ruleContext.setPlatform(getPlatform());
        ruleContext.setRuleConfig(appConfig.getRuleConfigMap().get(ruleContext.getPlatform()));
        ruleContext.setFileContextMap(new LinkedHashMap<>());
        ruleContext.setSkippedFileReasonMap(new LinkedHashMap<>());
        ruleContext.setAreaFinalMap(new LinkedHashMap<>());
        for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            ruleContext.getAreaFinalMap().put(areaConfig.getArea(), new LinkedHashSet<>());
        }
        ruleContext.setGlobalTagBlackList(appConfig.getGlobalConfig().getGlobalTagBlacklist());

        return ruleContext;
    }

    private Set<String> parseLicensedGames(String datFilePath) throws Exception {
        Set<String> licensed = new HashSet<>();
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(datFilePath));
        NodeList gameNodes = document.getElementsByTagName("game");
        for (int i = 0; i < gameNodes.getLength(); i++) {
            String name = ((Element) gameNodes.item(i)).getAttribute("name");
            if (!name.isEmpty()) {
                licensed.add(name);
            }
        }

        return licensed;
    }

    private void populateFileContextMap(RuleContext ruleContext, String romDirPath) throws Exception {
        File[] files = new File(romDirPath).listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
            }
            for (File file : files) {
                String previousRevision = previousRevision(file.getName());
                if (previousRevision != null) {
                    FileContext removedFileContext = ruleContext.getFileContextMap().remove(previousRevision);
                    if (removedFileContext != null) {
                        ruleContext.getSkippedFileReasonMap().put(previousRevision, "存在新版修订，已被替代: " + file.getName());
                    }
                }
            }
        }
    }

    private FileContext buildFileContext(String fileName) throws Exception {
        String fullName = fileName;
        if (fileName.contains(".")) {
            fullName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        String namePart = fullName;
        String tagPart = "";
        Set<String> tags = new HashSet<>();

        int firstParen = fullName.indexOf('(');
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
        Matcher matcher = REV_TAG.matcher(filename);
        if (!matcher.find()) {
            return null;
        }

        try {
            int revision = Integer.parseInt(matcher.group(1));
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

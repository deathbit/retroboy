package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RenameOption;
import com.github.deathbit.retroboy.domain.RenamePlan;
import com.github.deathbit.retroboy.domain.RenameFileInput;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.handler.component.RenameGameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RenameGameHandlerImpl implements RenameGameHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        ruleContext.setFilesToRename(buildRenameFileInputs(ruleContext));
        if (!ruleContext.getFilesToRename().isEmpty()) {
            fileComponent.batchRenameFiles(ruleContext.getFilesToRename());
        }
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

    private String findRenameOption(List<RenameOption> renameOptions, String fileName) {
        if (renameOptions == null || renameOptions.isEmpty()) {
            return null;
        }

        return renameOptions.stream()
                .filter(renameOption -> fileName.equals(renameOption.getOldName()))
                .map(RenameOption::getNewName)
                .findFirst()
                .orElse(null);
    }

    private void addDuplicateNameReport(List<RenamePlan> renamePlans, List<String> duplicateNameReportLines, List<RenameOption> renameOptions) {
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
}


//package com.github.deathbit.retroboy.handler.component.impl;
//
//import com.github.deathbit.retroboy.domain.RuleContext;
//import com.github.deathbit.retroboy.enums.Area;
//import com.github.deathbit.retroboy.domain.HandlerMediaTypes;
//import com.github.deathbit.retroboy.domain.MediaType;
//import com.github.deathbit.retroboy.handler.component.ReportHandler;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class ReportHandlerImpl implements ReportHandler {
//
//    @Override
//    public void handle(RuleContext ruleContext) throws Exception {
//        var reportDir = Paths.get("report");
//        Files.createDirectories(reportDir);
//        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
//            var area = areaConfig.getArea();
//            var lines = buildProcessingReportYamlLines(
//                    ruleContext,
//                    area,
//                    ruleContext.getAreaRenameReportMap().get(area),
//                    ruleContext.getAreaDuplicateNameReportMap().get(area),
//                    ruleContext.getAreaMissingMediaReportMap().get(area),
//                    ruleContext.getAreaFailureReportMap().get(area));
//            var reportFile = reportDir.resolve(ruleContext.getPlatform().name() + "-" + area.name() + ".yaml");
//            Files.write(reportFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//        }
//    }
//
//    private List<String> buildProcessingReportYamlLines(RuleContext ruleContext,
//                                                        Area area,
//                                                        List<String> renameReportLines,
//                                                        List<String> duplicateNameReportLines,
//                                                        Map<String, List<String>> missingMediaReportLines,
//                                                        List<String> failureReportLines) {
//        var finalFiles = ruleContext.getAreaFinalMap().get(area);
//        var passedCount = finalFiles.size();
//        var lines = new ArrayList<String>();
//        lines.add("platform: " + yamlValue(ruleContext.getPlatform().name()));
//        lines.add("area: " + yamlValue(area.name()));
//        lines.add("romDir: " + yamlValue(ruleContext.getRuleConfig().getRomDir()));
//        lines.add("summary:");
//        lines.add("  total: " + ruleContext.getFileContextMap().size());
//        lines.add("  passed: " + passedCount);
//        lines.add("  failed: " + (ruleContext.getFileContextMap().size() - passedCount));
//
//        var passedFiles = new ArrayList<String>();
//        for (var fileName : ruleContext.getFileContextMap().keySet()) {
//            if (finalFiles.contains(fileName)) {
//                passedFiles.add(fileName);
//            }
//        }
//        addYamlList(lines, "passed", passedFiles, "");
//        addYamlList(lines, "renamed", renameReportLines, "");
//        addYamlList(lines, "duplicateNames", duplicateNameReportLines, "");
//        addMissingMediaYaml(lines, missingMediaReportLines);
//
//        var failedFiles = new ArrayList<String>();
//        if (!failureReportLines.isEmpty()) {
//            failedFiles.addAll(failureReportLines);
//        } else {
//            for (var fileName : ruleContext.getFileContextMap().keySet()) {
//                if (!finalFiles.contains(fileName)) {
//                    failedFiles.add(fileName);
//                }
//            }
//        }
//        addYamlList(lines, "failed", failedFiles, "");
//
//        return lines;
//    }
//
//    private void addMissingMediaYaml(List<String> lines, Map<String, List<String>> missingMediaReportLines) {
//        if (!hasMissingMediaReportLines(missingMediaReportLines)) {
//            lines.add("missingMedia: {}");
//            return;
//        }
//
//        lines.add("missingMedia:");
//        for (MediaType mediaType : HandlerMediaTypes.MEDIA_TYPES) {
//            var missingMediaFiles = missingMediaReportLines.get(mediaType.name());
//            if (missingMediaFiles == null || missingMediaFiles.isEmpty()) {
//                continue;
//            }
//            lines.add("  " + yamlValue(mediaType.name()) + ":");
//            for (var missingMediaFile : missingMediaFiles) {
//                lines.add("    - " + yamlValue(missingMediaFile));
//            }
//        }
//    }
//
//    private void addYamlList(List<String> lines, String key, List<String> values, String indent) {
//        if (values == null || values.isEmpty()) {
//            lines.add(indent + key + ": []");
//            return;
//        }
//
//        lines.add(indent + key + ":");
//        for (var value : values) {
//            lines.add(indent + "  - " + yamlValue(value));
//        }
//    }
//
//    private String yamlValue(String value) {
//        return "\"" + value
//                .replace("\\", "\\\\")
//                .replace("\"", "\\\"")
//                + "\"";
//    }
//
//    private boolean hasMissingMediaReportLines(Map<String, List<String>> missingMediaReportLines) {
//        return missingMediaReportLines != null
//                && missingMediaReportLines.values().stream().anyMatch(lines -> lines != null && !lines.isEmpty());
//    }
//}
//

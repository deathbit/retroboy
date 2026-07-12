package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.HandlerMediaTypes;
import com.github.deathbit.retroboy.domain.MediaType;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.handler.Handler;
import com.github.deathbit.retroboy.handler.component.RuleContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

@Component
public class RuleContextInitializerImpl implements RuleContextInitializer {

    @Autowired
    private AppConfig appConfig;

    @Override
    public RuleContext handle(Handler handler) throws Exception {
        var ruleContext = initializeRuleContext(handler);
        ruleContext.setLicensed(parseLicensedGames(ruleContext.getRuleConfig().getDatFile()));
        populateFileContextMap(ruleContext, ruleContext.getRuleConfig().getRomDir());
        ruleContext.setRuleMap(handler.getRuleMap());
        ruleContext.setAreaRenameReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaDuplicateNameReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaFailureReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaMissingMediaReportMap(createEmptyAreaMissingMediaReportMap(ruleContext));
        return ruleContext;
    }

    @Override
    public void initializeArea(RuleContext ruleContext, Area area) {
        ruleContext.setCurrentArea(area);
        ruleContext.setCurrentAreaConfig(ruleContext.getRuleConfig().getTargetAreaConfigs()
                                                    .stream()
                                                    .filter(targetAreaConfig -> area == targetAreaConfig.getArea())
                                                    .findFirst().orElseThrow());
        if (CollectionUtils.isEmpty(ruleContext.getCurrentAreaConfig().getFileNameBlackList())) {
            ruleContext.getCurrentAreaConfig().setFileNameBlackList(new HashSet<>());
        }
    }

    @Override
    public void initializeFile(RuleContext ruleContext, FileContext file) {
        ruleContext.setFailureReasons(new ArrayList<>());
    }

    private RuleContext initializeRuleContext(Handler handler) {
        var ruleContext = new RuleContext();
        ruleContext.setPlatform(handler.getPlatform());
        ruleContext.setRuleConfig(appConfig.getRuleConfigMap().get(ruleContext.getPlatform()));
        ruleContext.setFileContextMap(new LinkedHashMap<>());
        ruleContext.setAreaFinalMap(new LinkedHashMap<>());
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            ruleContext.getAreaFinalMap().put(areaConfig.getArea(), new LinkedHashSet<>());
        }
        ruleContext.setGlobalTagBlackList(appConfig.getGlobalConfig().getGlobalTagBlacklist());
        ruleContext.setGlobalRomWhitelist(appConfig.getGlobalConfig().getGlobalRomWhitelist());
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

    private void populateFileContextMap(RuleContext ruleContext, String romDirPath) {
        var files = new File(romDirPath).listFiles();
        if (files == null) {
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        for (var file : files) {
            ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
        }
    }

    private FileContext buildFileContext(String fileName) {
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
                if (end == -1) {
                    break;
                }
                tags.add(tagPart.substring(start + 1, end));
                start = end + 1;
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
            for (MediaType mediaType : HandlerMediaTypes.MEDIA_TYPES) {
                mediaReportMap.put(mediaType.name(), new ArrayList<>());
            }
            areaReportMap.put(areaConfig.getArea(), mediaReportMap);
        }
        return areaReportMap;
    }
}


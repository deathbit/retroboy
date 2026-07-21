package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RenameOption;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.handler.component.RuleContextInitializer;
import com.github.deathbit.retroboy.rule.Rules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RuleContextInitializerImpl implements RuleContextInitializer {

    @Autowired
    private AppConfig appConfig;

    @Override
    public RuleContext handle(Platform platform) throws Exception {
        var ruleContext = new RuleContext();
        ruleContext.setPlatform(platform);
        ruleContext.setPlatformName(platform.name().toLowerCase());
        ruleContext.setGlobalConfig(appConfig.getGlobalConfig());
        ruleContext.setPlatformPackTaskConfig(appConfig.getPlatformPackTaskConfigMap().get(platform));
        ruleContext.setRenameOptionMap(ruleContext.getPlatformPackTaskConfig().getRenameOptions()
                .stream().collect(Collectors.toMap(RenameOption::getOldName, RenameOption::getNewName)));
        ruleContext.setLicensed(parseLicensedGames(String.format("%s\\platform\\%s\\dat\\%s.dat",
                appConfig.getGlobalConfig().getResourcesHomePath(), ruleContext.getPlatformName(), ruleContext.getPlatformName())));
        populateFileContextMap(ruleContext, String.format("%s\\platform\\%s\\roms",
                appConfig.getGlobalConfig().getResourcesHomePath(), ruleContext.getPlatformName()));
        ruleContext.setRuleMap(Map.of(Area.JPN, Rules.IS_JAPAN_BASE, Area.USA, Rules.IS_USA_BASE, Area.EUR, Rules.IS_EUROPE_BASE));
        ruleContext.setAreaPassMap(new HashMap<>());
        ruleContext.setAreaNotPassReportMap(new HashMap<>());

        return ruleContext;
    }

    private Set<String> parseLicensedGames(String datFilePath) throws Exception {
        ProgressBar pb = new ProgressBar("解析正版");
        var licensed = new HashSet<String>();
        var document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(datFilePath));
        var gameNodes = document.getElementsByTagName("game");
        pb.startTask(gameNodes.getLength());
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var name = ((Element) gameNodes.item(i)).getAttribute("name");
            if (!name.isEmpty()) {
                licensed.add(name);
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
        return licensed;
    }

    private void populateFileContextMap(RuleContext ruleContext, String romDirPath) {
        ProgressBar pb = new ProgressBar("解析文件");
        var files = new File(romDirPath).listFiles();
        if (files == null) {
            return;
        }

        ruleContext.setFileContextMap(new HashMap<>());
        Arrays.sort(files, Comparator.comparing(File::getName));
        pb.startTask(files.length);
        for (int i = 0; i < files.length; i++) {
            var file = files[i];
            ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }

    private FileContext buildFileContext(String fileName) {
        var fullName = fileName;
        var ext = "";
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fullName = fileName.substring(0, dotIndex);
            ext = fileName.substring(dotIndex);
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
                .extension(ext)
                .build();
    }
}

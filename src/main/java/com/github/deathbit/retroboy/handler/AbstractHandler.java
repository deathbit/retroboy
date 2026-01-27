package com.github.deathbit.retroboy.handler;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.HandlerInput;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractHandler implements Handler {

    @Override
    public void handle(HandlerInput handlerInput) throws Exception {
        RuleContext ruleContext = buildRuleContext(handlerInput);
        Map<Area, Rule> ruleMap = getRuleMap();

        for (Map.Entry<String, FileContext> entry : ruleContext.getFileContextMap().entrySet()) {
            String fileName = entry.getKey();
            FileContext fileContext = entry.getValue();

            for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
                Area area = areaConfig.getArea();
                Rule rule = ruleMap.get(area);

                if (rule.pass(ruleContext, fileContext)) {
                    ruleContext.getAreaFinalMap().get(area).add(fileName);
                }
            }
        }
    }

    private RuleContext buildRuleContext(HandlerInput handlerInput) throws Exception {
        RuleContext ruleContext = initializeRuleContext(handlerInput);
        ruleContext.setLicensed(parseLicensedGames(ruleContext.getRuleConfig().getDatFile()));
        populateFileContextMap(ruleContext, ruleContext.getRuleConfig().getRomDir());

        return ruleContext;
    }

    private RuleContext initializeRuleContext(HandlerInput handlerInput) {
        RuleContext ruleContext = new RuleContext();
        ruleContext.setPlatform(getPlatform());
        ruleContext.setRuleConfig(handlerInput.getAppConfig().getRuleConfigMap().get(ruleContext.getPlatform()));
        ruleContext.setFileContextMap(new HashMap<>());
        ruleContext.setAreaFinalMap(new HashMap<>());
        for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            ruleContext.getAreaFinalMap().put(areaConfig.getArea(), new HashSet<>());
        }
        ruleContext.setGlobalTagBlackList(handlerInput.getAppConfig().getGlobalConfig().getTagBlacklist());

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
            for (File file : files) {
                ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
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

    public abstract Map<Area, Rule> getRuleMap();

    public abstract Platform getPlatform();
}

package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.domain.*;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHandler implements Handler {

    private static final Pattern REV_TAG = Pattern.compile("\\(Rev\\s+(\\d+)\\)");

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

        handlerInput.getFileComponent().batchCleanDirs(List.of(Paths.get(handlerInput.getAppConfig().getGlobalConfig().getEsdeHome(), ruleContext.getRuleConfig().getTargetDirBase())));
        
        List<Path> dirsToCreate = ruleContext.getAreaFinalMap().entrySet().stream()
                .map(entry -> Paths.get(handlerInput.getAppConfig().getGlobalConfig().getEsdeHome(), ruleContext.getRuleConfig().getTargetDirBase(), entry.getKey().name()))
                .collect(java.util.stream.Collectors.toList());
        handlerInput.getFileComponent().batchCreateDirs(dirsToCreate);

        List<CopyFileInput> filesToCopy = new java.util.ArrayList<>();
        for (Map.Entry<Area, Set<String>> entry : ruleContext.getAreaFinalMap().entrySet()) {
            for (String fileName : entry.getValue()) {
                filesToCopy.add(CopyFileInput.builder()
                        .srcFile(Paths.get(ruleContext.getRuleConfig().getRomDir(), fileName))
                        .destDir(Paths.get(handlerInput.getAppConfig().getGlobalConfig().getEsdeHome(), ruleContext.getRuleConfig().getTargetDirBase(), entry.getKey().name()))
                        .build());
            }
        }
        handlerInput.getFileComponent().batchCopyFiles(filesToCopy);
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

        // Get original rule config
        RuleConfig originalConfig = handlerInput.getAppConfig().getRuleConfigMap().get(ruleContext.getPlatform());

        // Resolve paths using GlobalConfig
        var globalConfig = handlerInput.getAppConfig().getGlobalConfig();
        RuleConfig resolvedConfig = RuleConfig.builder()
                .platform(originalConfig.getPlatform())
                .datFile(java.nio.file.Paths.get(globalConfig.getResourcesHome(), originalConfig.getDatFile()).toString())
                .romDir(java.nio.file.Paths.get(globalConfig.getResourcesHome(), originalConfig.getRomDir()).toString())
                .targetDirBase(java.nio.file.Paths.get(globalConfig.getEsdeHome(), originalConfig.getTargetDirBase()).toString())
                .targetAreaConfigs(originalConfig.getTargetAreaConfigs())
                .tagBlackList(originalConfig.getTagBlackList())
                .fileNameBlackList(originalConfig.getFileNameBlackList())
                .build();

        ruleContext.setRuleConfig(resolvedConfig);
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
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
                String previousRevision = previousRevision(file.getName());
                if (previousRevision != null) {
                    ruleContext.getFileContextMap().remove(previousRevision);
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

    public abstract Map<Area, Rule> getRuleMap();

    public abstract Platform getPlatform();
}

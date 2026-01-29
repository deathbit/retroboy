package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
        ruleContext.setFileContextMap(new HashMap<>());
        ruleContext.setAreaFinalMap(new HashMap<>());
        for (AreaConfig areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            ruleContext.getAreaFinalMap().put(areaConfig.getArea(), new HashSet<>());
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

    @Override
    public Map<Area, Rule> getRuleMap() {
        return Map.of(
                Area.JPN, Rules.IS_JAPAN_BASE,
                Area.USA, Rules.IS_USA_BASE,
                Area.EUR, Rules.IS_EUROPE_BASE
        );
    }

    public abstract Platform getPlatform();
}

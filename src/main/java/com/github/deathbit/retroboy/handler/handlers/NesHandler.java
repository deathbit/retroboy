package com.github.deathbit.retroboy.handler.handlers;

import com.github.deathbit.retroboy.component.CopyComponent;
import com.github.deathbit.retroboy.component.CreateComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.domain.CopyFile;
import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.handler.Handler;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class NesHandler implements Handler {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private CreateComponent createComponent;

    @Autowired
    private ProgressBarComponent progressBarComponent;

    @Autowired
    private CopyComponent copyComponent;

    @Override
    public RuleContext buildRuleContext(RuleConfig ruleConfig, AppConfig appConfig) {
        Set<String> licensed = new HashSet<>();
        
        try {
            File datFile = new File(ruleConfig.getDatFile());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(datFile);
            
            // Get all game elements
            NodeList gameNodes = document.getElementsByTagName("game");
            
            // Extract name attribute from each game element
            for (int i = 0; i < gameNodes.getLength(); i++) {
                Element gameElement = (Element) gameNodes.item(i);
                String name = gameElement.getAttribute("name");
                if (!name.isEmpty()) {
                    licensed.add(name);
                }
            }
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e) {
            throw new RuntimeException("Failed to parse DAT file: " + ruleConfig.getDatFile(), e);
        }
        
        return RuleContext.builder()
                .licensed(licensed)
                .globalTagBlackList(appConfig.getGlobalTagBlackList())
                .build();
    }

    @Override
    public FileContext buildFileContext(String fileName) {
        // Extract full name (without extension)
        String fullName = fileName;
        if (fileName.contains(".")) {
            fullName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        
        // Parse name part and tag part
        String namePart = fullName;
        String tagPart = "";
        List<String> tags = new ArrayList<>();
        
        int firstParen = fullName.indexOf('(');
        if (firstParen != -1) {
            namePart = fullName.substring(0, firstParen).trim();
            tagPart = fullName.substring(firstParen);
            
            // Extract individual tags
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
    public List<Rule> buildJapanRuleChain() {
        return List.of(
                Rules.IS_JAPAN_LICENSED
        );
    }

    @Override
    public List<Rule> buildUsaRuleChain() {
        return List.of(
                Rules.IS_USA_LICENSED
        );
    }

    @Override
    public List<Rule> buildEuropeRuleChain() {
        return List.of(
                Rules.IS_EUROPE_LICENSED
        );
    }

    @Override
    public void handle() {
        RuleConfig ruleConfig = appConfig.getNesRuleConfig();
        RuleContext ruleContext = buildRuleContext(ruleConfig, appConfig);

        createComponent.createDir(ruleConfig.getJapanTargetDir());
        createComponent.createDir(ruleConfig.getUsaTargetDir());
        createComponent.createDir(ruleConfig.getEuropeTargetDir());

        // Initialize the japanFinal set
        Set<String> japanFinal = new HashSet<>();
        Set<String> usaFinal = new HashSet<>();
        Set<String> europeFinal = new HashSet<>();
        
        // Read all files from romDir
        File romDir = new File(ruleConfig.getRomDir());
        if (romDir.exists() && romDir.isDirectory()) {
            File[] files = romDir.listFiles();
            if (files != null) {
                // Get the Japan rule chain (same for all files)
                List<Rule> japanRuleChain = buildJapanRuleChain();
                List<Rule> usaRuleChain = buildUsaRuleChain();
                List<Rule> europeRuleChain = buildEuropeRuleChain();

                progressBarComponent.start("计算FC规则", files.length);

                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        
                        // Build FileContext for the file
                        FileContext fileContext = buildFileContext(fileName);
                        
                        // Apply Japan rule chain to determine if file should be added to japanFinal
                        boolean passJapanRules = japanRuleChain.stream()
                                .allMatch(rule -> rule.pass(ruleContext, fileContext));
                        boolean passUsaRules = usaRuleChain.stream()
                                .allMatch(rule -> rule.pass(ruleContext, fileContext));
                        boolean passEuropeRules = europeRuleChain.stream()
                                .allMatch(rule -> rule.pass(ruleContext, fileContext));
                        if (passJapanRules) {
                            japanFinal.add(fileName);
                        }
                        if (passUsaRules) {
                            usaFinal.add(fileName);
                        }
                        if (passEuropeRules) {
                            europeFinal.add(fileName);
                        }
                        progressBarComponent.update("计算规则：" + fileName);
                    }
                }
                progressBarComponent.finish();
            }
        }

        // Update the RuleContext with japanFinal
        ruleContext.setJapanFinal(japanFinal);
        ruleContext.setUsaFinal(usaFinal);
        ruleContext.setEuropeFinal(europeFinal);

        // Copy files to their respective target directories
        List<CopyFile> allCopyFiles = new ArrayList<>();
        
        // Prepare Japan files for copying
        for (String fileName : japanFinal) {
            String srcFilePath = new File(ruleConfig.getRomDir(), fileName).getAbsolutePath();
            allCopyFiles.add(CopyFile.builder()
                    .srcFile(srcFilePath)
                    .destDir(ruleConfig.getJapanTargetDir())
                    .build());
        }
        
        // Prepare USA files for copying
        for (String fileName : usaFinal) {
            String srcFilePath = new File(ruleConfig.getRomDir(), fileName).getAbsolutePath();
            allCopyFiles.add(CopyFile.builder()
                    .srcFile(srcFilePath)
                    .destDir(ruleConfig.getUsaTargetDir())
                    .build());
        }
        
        // Prepare Europe files for copying
        for (String fileName : europeFinal) {
            String srcFilePath = new File(ruleConfig.getRomDir(), fileName).getAbsolutePath();
            allCopyFiles.add(CopyFile.builder()
                    .srcFile(srcFilePath)
                    .destDir(ruleConfig.getEuropeTargetDir())
                    .build());
        }
        
        // Batch copy all files
        copyComponent.batchCopyFile(allCopyFiles);

        System.out.println();
    }
}

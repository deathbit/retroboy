//package com.github.deathbit.retroboy.handler.handlers;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//
//import com.github.deathbit.retroboy.component.FileComponent;
//import com.github.deathbit.retroboy.config.AppConfig;
//import com.github.deathbit.retroboy.domain.RuleConfig;
//import com.github.deathbit.retroboy.handler.AbstractHandler;
//import com.github.deathbit.retroboy.rule.Rule;
//import com.github.deathbit.retroboy.domain.FileContext;
//import com.github.deathbit.retroboy.domain.RuleContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//
//@Component
//public class NesHandler extends AbstractHandler {
//
//    @Autowired
//    private AppConfig appConfig;
//
//    @Autowired
//    private FileComponent fileComponent;
//
//    @Override
//    public RuleContext buildRuleContext(RuleConfig ruleConfig, AppConfig appConfig) {

//    }
//
//    @Override
//    public void handle() {
//        RuleConfig ruleConfig = appConfig.getNesRuleConfig();
//        RuleContext ruleContext = buildRuleContext(ruleConfig, appConfig);
//
//        fileComponent.createDir(ruleConfig.getJapanTargetDir());
//        fileComponent.createDir(ruleConfig.getUsaTargetDir());
//        fileComponent.createDir(ruleConfig.getEuropeTargetDir());
//
//        // Initialize the japanFinal set
//        Set<String> japanFinal = new HashSet<>();
//        Set<String> usaFinal = new HashSet<>();
//        Set<String> europeFinal = new HashSet<>();
//
//        // Read all files from romDir
//        File romDir = new File(ruleConfig.getRomDir());
//        if (romDir.exists() && romDir.isDirectory()) {
//            File[] files = romDir.listFiles();
//            if (files != null) {
//                // Get the Japan rule chain (same for all files)
//                Rule japanRule = buildJapanRule();
//                Rule usaRule = buildUsaRule();
//                Rule europeRule = buildEuropeRule();
//
//                progressBarComponent.start("计算FC规则", files.length);
//
//                for (File file : files) {
//                    if (file.isFile()) {
//                        String fileName = file.getName();
//
//                        // Build FileContext for the file
//                        FileContext fileContext = buildFileContext(fileName);
//
//                        // Apply Japan rule chain to determine if file should be added to japanFinal
//                        boolean passJapanRules = japanRule.pass(ruleContext, fileContext);
//                        boolean passUsaRules = usaRule.pass(ruleContext, fileContext);
//                        boolean passEuropeRules = europeRule.pass(ruleContext, fileContext);
//                        if (passJapanRules) {
//                            japanFinal.add(fileName);
//                        }
//                        if (passUsaRules) {
//                            usaFinal.add(fileName);
//                        }
//                        if (passEuropeRules) {
//                            europeFinal.add(fileName);
//                        }
//                        progressBarComponent.update("计算规则：" + fileName);
//                    }
//                }
//                progressBarComponent.finish();
//            }
//        }
//
//        // Update the RuleContext with japanFinal
//        ruleContext.setJapanFinal(japanFinal);
//        ruleContext.setUsaFinal(usaFinal);
//        ruleContext.setEuropeFinal(europeFinal);
//
//        // Copy files to their respective target directories
//        List<CopyFile> allCopyFiles = new ArrayList<>();
//
//        // Prepare Japan files for copying
//        for (String fileName : japanFinal) {
//            String srcFilePath = new File(ruleConfig.getRomDir(), fileName).getAbsolutePath();
//            allCopyFiles.add(CopyFile.builder()
//                                     .srcFile(srcFilePath)
//                                     .destDir(ruleConfig.getJapanTargetDir())
//                                     .build());
//        }
//
//        // Prepare USA files for copying
//        for (String fileName : usaFinal) {
//            String srcFilePath = new File(ruleConfig.getRomDir(), fileName).getAbsolutePath();
//            allCopyFiles.add(CopyFile.builder()
//                                     .srcFile(srcFilePath)
//                                     .destDir(ruleConfig.getUsaTargetDir())
//                                     .build());
//        }
//
//        // Prepare Europe files for copying
//        for (String fileName : europeFinal) {
//            String srcFilePath = new File(ruleConfig.getRomDir(), fileName).getAbsolutePath();
//            allCopyFiles.add(CopyFile.builder()
//                                     .srcFile(srcFilePath)
//                                     .destDir(ruleConfig.getEuropeTargetDir())
//                                     .build());
//        }
//
//        // Batch copy all files
//        fileComponent.batchCopyFile(allCopyFiles);
//    }
//}

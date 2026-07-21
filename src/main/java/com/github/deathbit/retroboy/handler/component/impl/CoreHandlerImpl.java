//package com.github.deathbit.retroboy.handler.component.impl;
//
//import com.github.deathbit.retroboy.config.AppConfig;
//import com.github.deathbit.retroboy.domain.RuleContext;
//import com.github.deathbit.retroboy.handler.component.CoreHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Component
//public class CoreHandlerImpl implements CoreHandler {
//
//    private static final Path DEFAULT_RETRO_ARCH_RESOURCE_DIR = Paths.get("D:\\Resources\\RetroArch-Win64");
//
//    @Autowired
//    private AppConfig appConfig;
//
//    @Override
//    public void handle(RuleContext ruleContext) throws Exception {
//        var defaultCore = ruleContext.getRuleConfig().getDefaultCore();
//        var retroArchDir = Paths.get(appConfig.getGlobalConfig().getRaConfig()).getParent();
//        var coreConfigDir = retroArchDir.resolve("config").resolve(defaultCore);
//        copyCoreConfigFile(retroArchDir, coreConfigDir.resolve(defaultCore + ".opt"));
//        copyCoreConfigFile(retroArchDir, coreConfigDir.resolve(defaultCore + ".slangp"));
//    }
//
//    private void copyCoreConfigFile(Path retroArchDir, Path targetFile) throws Exception {
////        var sourceFile = resolveRetroArchResourceDir(retroArchDir).resolve(retroArchDir.relativize(targetFile));
////        Files.createDirectories(targetFile.getParent());
////        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
//    }
//
////    private Path resolveRetroArchResourceDir(Path retroArchDir) {
////        var defaultConfigTask = appConfig.getDefaultConfigTask();
////        if (defaultConfigTask == null || defaultConfigTask.getCopyDirContentsInputs() == null) {
////            return DEFAULT_RETRO_ARCH_RESOURCE_DIR;
////        }
////
////        return defaultConfigTask.getCopyDirContentsInputs()
////                                .stream()
////                                .filter(input -> {
////                                    var destDir = Paths.get(input.getDestDir());
////                                    return destDir.getParent() != null && destDir.getParent().equals(retroArchDir);
////                                })
////                                .map(input -> Paths.get(input.getSrcDir()).getParent())
////                                .findFirst()
////                                .orElse(DEFAULT_RETRO_ARCH_RESOURCE_DIR);
////    }
//}

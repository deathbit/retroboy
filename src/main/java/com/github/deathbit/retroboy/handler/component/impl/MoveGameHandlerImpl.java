package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.MoveGameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class MoveGameHandlerImpl implements MoveGameHandler {

    @Autowired
    private FileComponent fileComponent;

    @Autowired
    private AppConfig appConfig;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        fileComponent.batchCleanDirs(List.of(ruleContext.getRuleConfig().getTargetDirBase()));
        ruleContext.setDirsToCreate(ruleContext.getAreaFinalMap().keySet().stream()
                                               .map(areaKey -> Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), areaKey.name()).toString())
                                               .toList());
        fileComponent.batchCreateDirs(ruleContext.getDirsToCreate());
        var filesToCopy = new ArrayList<CopyFileInput>();
        for (var entry : ruleContext.getAreaFinalMap().entrySet()) {
            for (var fileName : entry.getValue()) {
                filesToCopy.add(CopyFileInput.builder()
                                             .srcFile(Paths.get(ruleContext.getRuleConfig().getRomDir(), fileName).toString())
                                             .destDir(Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), entry.getKey().name()).toString())
                                             .build());
            }
        }
        ruleContext.setFilesToCopy(filesToCopy);
        fileComponent.batchCopyFiles(ruleContext.getFilesToCopy());
        var romDirName = ruleContext.getRuleConfig().getRomDirName();
        fileComponent.batchCopyFiles(List.of(CopyFileInput.builder()
                                                          .srcFile(Paths.get(appConfig.getGlobalConfig().getRomsAllDir(), romDirName, "systeminfo.txt").toString())
                                                          .destDir(Paths.get(appConfig.getGlobalConfig().getRomsDir(), romDirName).toString())
                                                          .build()));
    }
}

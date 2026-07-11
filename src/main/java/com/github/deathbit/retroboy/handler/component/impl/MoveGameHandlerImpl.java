package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
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
        var targetDirBase = Paths.get(ruleContext.getRuleConfig().getTargetDirBase());
        var targetPlatformDirName = targetDirBase.getFileName().toString();
        var romsAllDir = targetDirBase.getParent().resolveSibling("ROMs_ALL");
        fileComponent.batchCopyFiles(List.of(CopyFileInput.builder()
                                                          .srcFile(romsAllDir.resolve(targetPlatformDirName).resolve("systeminfo.txt").toString())
                                                          .destDir(targetDirBase.toString())
                                                          .build()));
    }
}

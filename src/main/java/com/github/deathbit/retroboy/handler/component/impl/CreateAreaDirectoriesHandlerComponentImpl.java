package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.CreateAreaDirectoriesHandlerComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class CreateAreaDirectoriesHandlerComponentImpl implements CreateAreaDirectoriesHandlerComponent {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        ruleContext.setDirsToCreate(ruleContext.getAreaFinalMap().keySet().stream()
                .map(areaKey -> Paths.get(ruleContext.getRuleConfig().getTargetDirBase(), areaKey.name()).toString())
                .toList());
        fileComponent.batchCreateDirs(ruleContext.getDirsToCreate());
    }
}


package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.MoveGameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoveGameHandlerImpl implements MoveGameHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        ruleContext.getAreaPassMap().forEach((area, roms) -> {
            String targetPath = String.format("%s\\ROMs\\%s\\%s", ruleContext.getGlobalConfig().getEsdeHomePath(),
                    ruleContext.getPlatformName(), area.name());
            fileComponent.deletePath(targetPath);
        });
        ruleContext.getAreaPassMap().forEach((area, roms) -> {
            ProgressBar pb = new ProgressBar("复制游戏");
            pb.startTask(roms.size());
            String targetPath = String.format("%s\\ROMs\\%s\\%s", ruleContext.getGlobalConfig().getEsdeHomePath(),
                    ruleContext.getPlatformName(), area.name());
            for (int i = 0; i < roms.size(); i++) {
                var rom = roms.get(i);
                fileComponent.copyPath(PathPair.builder()
                        .sourcePath(String.format("%s\\platform\\%s\\roms\\%s",
                                ruleContext.getGlobalConfig().getResourcesHomePath(), ruleContext.getPlatformName(), rom))
                        .targetPath(targetPath)
                        .build());
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        });
    }
}

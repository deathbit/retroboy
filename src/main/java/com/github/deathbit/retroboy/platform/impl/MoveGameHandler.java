package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoveGameHandler {

    @Autowired
    private FileComponent fileComponent;
    public void handle(RuleContext ruleContext) throws Exception {
        ruleContext.getAreaPassMap().forEach((area, roms) -> {
            var targetPath = PathUtils.esdeAreaRomDirectory(ruleContext, area);
            fileComponent.deletePath(targetPath);
        });
        ruleContext.getAreaPassMap().forEach((area, roms) -> {
            ProgressBar pb = new ProgressBar("复制游戏");
            pb.startTask(roms.size());
            var targetPath = PathUtils.esdeAreaRomDirectory(ruleContext, area);
            for (int i = 0; i < roms.size(); i++) {
                var rom = roms.get(i);
                fileComponent.copyPath(PathPair.builder()
                                               .sourcePath(PathUtils.platformRom(ruleContext, rom))
                                               .targetPath(targetPath)
                                               .build());
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        });
    }
}

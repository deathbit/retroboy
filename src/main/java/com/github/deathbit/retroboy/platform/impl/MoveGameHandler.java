package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoveGameHandler {

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) throws Exception {
        platformContext.getGameDBsByArea().forEach((area, gameDbs) -> {
            var targetPath = PathUtils.esdeAreaRomDirectory(platformContext, area);
            fileComponent.deletePath(targetPath);
        });
        platformContext.getGameDBsByArea().forEach((area, gameDbs) -> {
            ProgressBar pb = new ProgressBar("复制游戏");
            pb.startTask(gameDbs.size());
            var targetPath = PathUtils.esdeAreaRomDirectory(platformContext, area);
            for (int i = 0; i < gameDbs.size(); i++) {
                var gameDB = gameDbs.get(i);
                var fileContext = platformContext.getFileContextMap().get(gameDB.getRomName());
                if (fileContext == null) {
                    throw new RuntimeException("FileContext not found for rom: " + gameDB.getRomName());
                }
                fileComponent.copyPath(PathPair.builder()
                                               .sourcePath(PathUtils.platformRom(platformContext, fileContext.getFileName()))
                                               .targetPath(targetPath)
                                               .build());
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        });
    }
}

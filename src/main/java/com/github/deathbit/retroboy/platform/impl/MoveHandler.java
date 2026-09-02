package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.util.FileContextUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MoveHandler {

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) throws Exception {
        var gameDBsByArea = buildGameDBsByArea(platformContext);
        var fileContextLookupMap = FileContextUtils.buildLookupMap(platformContext.getFileContexts());
        gameDBsByArea.forEach((area, gameDbs) -> {
            var targetPath = PathUtils.esdeAreaRomDirectory(platformContext, area);
            fileComponent.deletePath(targetPath);
        });
        gameDBsByArea.forEach((area, gameDbs) -> {
            ProgressBar pb = new ProgressBar("复制游戏");
            pb.startTask(gameDbs.size());
            var targetPath = PathUtils.esdeAreaRomDirectory(platformContext, area);
            for (int i = 0; i < gameDbs.size(); i++) {
                var gameDB = gameDbs.get(i);
                var fileContext = FileContextUtils.requireFileContext(fileContextLookupMap, gameDB.getTitle());
                fileComponent.copyPath(PathPair.builder()
                                               .sourcePath(PathUtils.platformRom(platformContext, fileContext.getFileName()))
                                               .targetPath(targetPath)
                                               .build());
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        });
    }

    private Map<String, List<NoIntroGame>> buildGameDBsByArea(PlatformContext platformContext) {
        var gameDBsByArea = new LinkedHashMap<String, List<NoIntroGame>>();
        for (var pkg : platformContext.getNoIntroGamePackages()) {
            pkg.getNoIntroGameByArea().forEach((area, gameDB) ->
                gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB));
        }
        return gameDBsByArea;
    }
}

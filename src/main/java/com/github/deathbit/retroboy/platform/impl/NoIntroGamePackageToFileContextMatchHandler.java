package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.util.FileContextUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
public class NoIntroGamePackageToFileContextMatchHandler {

    public void handle(PlatformContext platformContext) {
        var matchResults = requireMatchResults(platformContext);
        var fileContextLookupMap = FileContextUtils.buildLookupMap(platformContext.getFileContexts());

        for (var matchResult : matchResults) {
            var fileContextByArea = new LinkedHashMap<String, FileContext>();
            for (var entry : matchResult.getNoIntroGamePackage().getNoIntroGameByArea().entrySet()) {
                var area = entry.getKey();
                var noIntroGame = entry.getValue();
                var fileContext = FileContextUtils.requireFileContext(fileContextLookupMap, noIntroGame.getTitle());
                fileContextByArea.put(area, fileContext);
            }
            matchResult.setFileContextByArea(fileContextByArea);
        }
    }

    private List<MatchResult> requireMatchResults(PlatformContext platformContext) {
        var matchResults = platformContext.getMatchResults();
        if (matchResults == null) {
            throw new RuntimeException("matchResults is null, run WikiGamePackageToNoIntroGamePackageMatchHandler before NoIntroGamePackageToFileContextMatchHandler");
        }
        return matchResults;
    }
}


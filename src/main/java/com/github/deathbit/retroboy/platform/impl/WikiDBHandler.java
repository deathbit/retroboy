package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.WikiDB;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WikiDBHandler {

    public void handle(PlatformContext platformContext) throws Exception {
        var wikiDBPackages = platformContext.getPlatformProcessor().processWiki();
        platformContext.setWikiDBPackages(wikiDBPackages);

        var wikiDBsByArea = new LinkedHashMap<String, List<WikiDB>>();
        var wikiDBById = new LinkedHashMap<String, WikiDB>();
        var wikiDBByName = new LinkedHashMap<String, WikiDB>();

        for (var pkg : wikiDBPackages) {
            for (var entry : pkg.getWikiDBByArea().entrySet()) {
                var area = entry.getKey();
                var wikiDB = entry.getValue();
                wikiDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(wikiDB);
                wikiDBById.putIfAbsent(wikiDB.getId(), wikiDB);
                wikiDBByName.putIfAbsent(wikiDB.getName(), wikiDB);
            }
        }

        platformContext.setWikiDBsByArea(wikiDBsByArea);
        platformContext.setWikiDBAreas(new ArrayList<>(wikiDBsByArea.keySet()));
        platformContext.setWikiDBById(wikiDBById);
        platformContext.setWikiDBByName(wikiDBByName);
        platformContext.setWikiDBs(new ArrayList<>(wikiDBById.values()));
        platformContext.setWikiDBPackageById(wikiDBPackages.stream().collect(Collectors.toMap(WikiDBPackage::getId, Function.identity())));
    }
}

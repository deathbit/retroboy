package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.wiki.WikiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WikiHandler {

    @Autowired
    private Map<Platform, WikiParser> wikiParserMap;

    public void handle(PlatformContext platformContext) throws Exception {
        platformContext.setWikiGamePackages(wikiParserMap.get(platformContext.getPlatform()).parseWiki(platformContext));
    }
}

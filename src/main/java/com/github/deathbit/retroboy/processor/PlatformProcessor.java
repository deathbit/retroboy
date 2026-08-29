package com.github.deathbit.retroboy.processor;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.Platform;

import java.util.List;
import java.util.Map;

public interface PlatformProcessor {

    Platform platform();

    void preProcessGameDB(Map<String, GameDB> gameDBMapByRomName);

    List<WikiDBPackage> processWiki() throws Exception;

    Map<String, String> gameDBToWikiDBAreaMapping(List<String> gameDBAreas, List<String> wikiDBAreas);
}

package com.github.deathbit.retroboy.wiki;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.Platform;

import java.util.List;

public interface WikiParser {

    Platform getPlatform();

    List<WikiGamePackage> parseWiki(PlatformContext platformContext) throws Exception;
}

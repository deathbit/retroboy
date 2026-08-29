package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

@Component
public class PlatformContextInitializer {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    public PlatformContext handle(Platform platform) throws Exception {
        var platformContext = new PlatformContext();
        // set platform
        platformContext.setPlatform(platform);
        platformContext.setPlatformName(platform.name().toLowerCase());
        platformContext.setPlatformProcessor(platformProcessorMap.get(platform));
        // set config
        platformContext.setAppConfig(appConfig);
        platformContext.setGlobalConfig(appConfig.getGlobalConfig());
        platformContext.setPlatformPackTaskConfig(appConfig.getPlatformPackTaskConfigMap().get(platform));
        // set game db
        platformContext.setGameDBs(parseGameDBList(platformContext.getPlatformName()));
        platformContext.setGameDBMapByRomName(platformContext.getGameDBs().stream().collect(Collectors.toMap(GameDB::getRomName, Function.identity())));
        platformContext.setGameDBMapByNumber(platformContext.getGameDBs().stream().collect(Collectors.toMap(GameDB::getNumber, Function.identity())));
        // set file context map
        platformContext.setFileContextMap(parseFileContextMap(PathUtils.string(PathUtils.PLATFORM_ROMS, platformContext)));

        return platformContext;
    }

    private List<GameDB> parseGameDBList(String platformName) throws Exception {
        ProgressBar pb = new ProgressBar("解析游戏库");
        var gameDBList = new ArrayList<GameDB>();
        var gameDBResource = new ClassPathResource("platform/%s/%s_db.xml".formatted(platformName, platformName));
        var content = readResourceAsString(gameDBResource)
            .replaceFirst("^\\s*<\\?xml[^?]*\\?>", "");
        var document = createDocumentBuilderFactory()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader("<root>" + content + "</root>")));
        var gameNodes = document.getElementsByTagName("game");
        pb.startTask(gameNodes.getLength());
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var gameElement = (Element) gameNodes.item(i);
            var romName = gameElement.getAttribute("name");
            var childNodes = gameElement.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j) instanceof Element archiveElement
                    && "archive".equals(archiveElement.getTagName())) {
                    gameDBList.add(buildGameDB(romName, archiveElement));
                }
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
        return gameDBList;
    }

    private String readResourceAsString(ClassPathResource resource) throws Exception {
        if (!resource.exists()) {
            throw new IllegalArgumentException("游戏库资源不存在: " + resource.getPath());
        }
        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            var content = new StringBuilder();
            var buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
            return content.toString();
        }
    }

    private GameDB buildGameDB(String romName, Element archiveElement) {
        return GameDB.builder()
                     .romName(romName)
                     .additional(archiveElement.getAttribute("additional"))
                     .adult(archiveElement.getAttribute("adult"))
                     .aftermarket(archiveElement.getAttribute("aftermarket"))
                     .alt(archiveElement.getAttribute("alt"))
                     .bios(archiveElement.getAttribute("bios"))
                     .categories(archiveElement.getAttribute("categories"))
                     .clone(archiveElement.getAttribute("clone"))
                     .complete(archiveElement.getAttribute("complete"))
                     .dat(archiveElement.getAttribute("dat"))
                     .datter_note(archiveElement.getAttribute("datter_note"))
                     .description(archiveElement.getAttribute("description"))
                     .devstatus(archiveElement.getAttribute("devstatus"))
                     .langchecked(archiveElement.getAttribute("langchecked"))
                     .languages(archiveElement.getAttribute("languages"))
                     .licensed(archiveElement.getAttribute("licensed"))
                     .listed(archiveElement.getAttribute("listed"))
                     .name(archiveElement.getAttribute("name"))
                     .name_alt(archiveElement.getAttribute("name_alt"))
                     .number(archiveElement.getAttribute("number"))
                     .physical(archiveElement.getAttribute("physical"))
                     .region(archiveElement.getAttribute("region"))
                     .regparent(archiveElement.getAttribute("regparent"))
                     .showlang(archiveElement.getAttribute("showlang"))
                     .special1(archiveElement.getAttribute("special1"))
                     .special2(archiveElement.getAttribute("special2"))
                     .sticky_note(archiveElement.getAttribute("sticky_note"))
                     .version1(archiveElement.getAttribute("version1"))
                     .version2(archiveElement.getAttribute("version2"))
                     .build();
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private Map<String, FileContext> parseFileContextMap(String romDirPath) {
        ProgressBar pb = new ProgressBar("解析文件");
        var files = new File(romDirPath).listFiles();
        if (files == null) {
            return new HashMap<>();
        }

        var fileContextMap = new HashMap<String, FileContext>();
        Arrays.sort(files, Comparator.comparing(File::getName));
        pb.startTask(files.length);
        for (int i = 0; i < files.length; i++) {
            var file = files[i];
            var fileContext = buildFileContext(file.getName());
            fileContextMap.put(fileContext.getFullName(), fileContext);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();

        return fileContextMap;
    }

    private FileContext buildFileContext(String fileName) {
        var fullName = fileName;
        var ext = "";
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fullName = fileName.substring(0, dotIndex);
            ext = fileName.substring(dotIndex);
        }

        var namePart = fullName;
        var tagPart = "";
        var tags = new HashSet<String>();

        var firstParen = fullName.indexOf('(');
        if (firstParen != -1) {
            namePart = fullName.substring(0, firstParen).trim();
            tagPart = fullName.substring(firstParen);
            int start = 0;
            while ((start = tagPart.indexOf('(', start)) != -1) {
                int end = tagPart.indexOf(')', start);
                if (end == -1) {
                    break;
                }
                tags.add(tagPart.substring(start + 1, end));
                start = end + 1;
            }
        }

        return FileContext.builder()
                          .fileName(fileName)
                          .fullName(fullName)
                          .namePart(namePart)
                          .tagPart(tagPart)
                          .tags(tags)
                          .extension(ext)
                          .build();
    }
}

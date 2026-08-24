package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RenameOption;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rules;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RuleContextInitializer {

    @Autowired
    private AppConfig appConfig;
    public RuleContext handle(Platform platform) throws Exception {
        var ruleContext = new RuleContext();
        ruleContext.setPlatform(platform);
        ruleContext.setPlatformName(platform.name().toLowerCase());
        ruleContext.setGlobalConfig(appConfig.getGlobalConfig());
        ruleContext.setBasePackReleaseReportTaskConfig(appConfig.getBasePackReleaseReportTaskConfig());
        ruleContext.setPlatformPackTaskConfig(appConfig.getPlatformPackTaskConfigMap().get(platform));
        ruleContext.setRenameOptionMap(ruleContext.getPlatformPackTaskConfig().getRenameOptions()
                .stream().collect(Collectors.toMap(RenameOption::getOldName, RenameOption::getNewName)));
        ruleContext.setGameDBList(parseGameDBList(PathUtils.PLATFORM_GAME_DB.get(ruleContext)));
        ruleContext.setLicensed(parseLicensedGames(PathUtils.string(PathUtils.PLATFORM_DAT, ruleContext)));
        populateFileContextMap(ruleContext, PathUtils.string(PathUtils.PLATFORM_ROMS, ruleContext));
        ruleContext.setRuleMap(Map.of(Area.JPN, Rules.IS_JAPAN_BASE, Area.USA, Rules.IS_USA_BASE, Area.EUR, Rules.IS_EUROPE_BASE));
        ruleContext.setAreaPassMap(new HashMap<>());
        ruleContext.setAreaRuleResultMap(new HashMap<>());
        ruleContext.setAreaRenameResultMap(new HashMap<>());
        ruleContext.setAreaWikiEntryMap(new HashMap<>());

        return ruleContext;
    }

    private List<GameDB> parseGameDBList(Path gameDBPath) throws Exception {
        ProgressBar pb = new ProgressBar("解析游戏库");
        var gameDBList = new ArrayList<GameDB>();
        var content = Files.readString(gameDBPath)
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

    private Set<String> parseLicensedGames(String datFilePath) throws Exception {
        ProgressBar pb = new ProgressBar("解析正版");
        var licensed = new HashSet<String>();
        var document = createDocumentBuilderFactory()
                .newDocumentBuilder()
                .parse(new File(datFilePath));
        var gameNodes = document.getElementsByTagName("game");
        pb.startTask(gameNodes.getLength());
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var name = ((Element) gameNodes.item(i)).getAttribute("name");
            if (!name.isEmpty()) {
                licensed.add(name);
            }
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
        return licensed;
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private void populateFileContextMap(RuleContext ruleContext, String romDirPath) {
        ProgressBar pb = new ProgressBar("解析文件");
        var files = new File(romDirPath).listFiles();
        if (files == null) {
            return;
        }

        ruleContext.setFileContextMap(new HashMap<>());
        Arrays.sort(files, Comparator.comparing(File::getName));
        pb.startTask(files.length);
        for (int i = 0; i < files.length; i++) {
            var file = files[i];
            ruleContext.getFileContextMap().put(file.getName(), buildFileContext(file.getName()));
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
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

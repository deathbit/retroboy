package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.game.NoIntroGame;
import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

@Component
public class NoIntroHandler {

    private static final Pattern REGPARENT_AREA_PATTERN = Pattern.compile("\\(\\s*([A-Z0-9]+)\\s+PARENT\\s*\\)");
    private static final List<String> BASE_AREAS = List.of("USA", "JPN", "EUR");

    public void handle(PlatformContext platformContext) throws Exception {
        var noIntroGames = parseGameDBList(platformContext.getPlatformName());
        var noIntroGameByTitle = noIntroGames.stream()
                                             .collect(Collectors.toMap(NoIntroGame::getTitle, game -> game));
        platformContext.getPlatformProcessor().preProcessGameDB(noIntroGameByTitle);

        var filteredNoIntroGames = noIntroGames.stream()
                                               .filter(gameDB -> gameDB.getLicensed().isEmpty())
                                               .filter(gameDB -> gameDB.getBios().isEmpty())
                                               .filter(gameDB -> gameDB.getDevstatus().isEmpty())
                                               .filter(gameDB -> gameDB.getPhysical().isEmpty())
                                               .filter(gameDB -> gameDB.getRegparent().contains("PARENT"))
                                               .toList();

        var gameDBsByArea = new LinkedHashMap<String, List<NoIntroGame>>();
        for (var gameDB : filteredNoIntroGames) {
            var areas = new ArrayList<String>();
            for (var area : extractRegparentAreas(gameDB.getRegparent())) {
                if (shouldAddToArea(platformContext, area, gameDB)) {
                    gameDBsByArea.computeIfAbsent(area, ignored -> new ArrayList<>()).add(gameDB);
                    areas.add(area);
                }
            }
            gameDB.setAreas(areas);
        }

        // Build game DB packages: group GameDB entries by their root node (clone == "P")
        var rootToAreaGameDB = new LinkedHashMap<String, Map<String, NoIntroGame>>();
        for (var entry : gameDBsByArea.entrySet()) {
            var area = entry.getKey();
            for (var gameDB : entry.getValue()) {
                var rootNumber = "P".equals(gameDB.getClone()) ? gameDB.getId() : gameDB.getClone();
                var areaGameDB = rootToAreaGameDB.computeIfAbsent(rootNumber, ignored -> new LinkedHashMap<>());
                var existingGameDB = areaGameDB.get(area);
                if (existingGameDB != null) {
                    throw new RuntimeException("GameDB package area conflict: rootNumber=%s, area=%s, existing=%s(id=%s), duplicate=%s(id=%s)"
                        .formatted(
                            rootNumber,
                            area,
                            existingGameDB.getTitle(),
                            existingGameDB.getId(),
                            gameDB.getTitle(),
                            gameDB.getId()
                        ));
                }
                areaGameDB.put(area, gameDB);
            }
        }

        var gameDBPackages = new ArrayList<NoIntroGamePackage>();
        for (var entry : rootToAreaGameDB.entrySet()) {
            var packageId = entry.getKey();
            entry.getValue().forEach((area, gameDB) -> {
                gameDB.setPackageId(packageId);
            });
            gameDBPackages.add(NoIntroGamePackage.builder()
                                                 .id(packageId)
                                                 .noIntroGameByArea(entry.getValue())
                                                 .build());
        }
        platformContext.setNoIntroGamePackages(gameDBPackages);
    }

    private List<NoIntroGame> parseGameDBList(String platformName) throws Exception {
        ProgressBar pb = new ProgressBar("解析游戏");
        var gameDBList = new ArrayList<NoIntroGame>();
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
            var title = gameElement.getAttribute("name");
            var childNodes = gameElement.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j) instanceof Element archiveElement
                    && "archive".equals(archiveElement.getTagName())) {
                    gameDBList.add(buildGameDB(title, archiveElement));
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

    private NoIntroGame buildGameDB(String title, Element archiveElement) {
        return NoIntroGame.builder()
                          .id(archiveElement.getAttribute("number"))
                          .title(title)
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

    private List<String> extractRegparentAreas(String regparent) {
        var areas = new LinkedHashSet<String>();
        var matcher = REGPARENT_AREA_PATTERN.matcher(regparent);
        while (matcher.find()) {
            areas.add(matcher.group(1));
        }
        return new ArrayList<>(areas);
    }

    private boolean shouldAddToArea(PlatformContext platformContext, String area, NoIntroGame noIntroGame) {
        var config = platformContext.getPlatformPackTaskConfig();
        if (isAreaGameListed(config.getAreaGameBlackList(), area, noIntroGame)) {
            return false;
        }
        if (isAreaGameListed(config.getAreaGameWhiteList(), area, noIntroGame)) {
            return true;
        }
        return BASE_AREAS.contains(area) || "P".equals(noIntroGame.getClone());
    }

    private boolean isAreaGameListed(Set<String> list, String area, NoIntroGame noIntroGame) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return list.contains(area + " - " + noIntroGame.getTitle());
    }
}

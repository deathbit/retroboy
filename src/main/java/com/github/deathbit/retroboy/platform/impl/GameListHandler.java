package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.MatchResult;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Component
public class GameListHandler {

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) throws Exception {
        var gamelistXml = PathUtils.PLATFORM_GAMELIST_XML.get(platformContext);
        fileComponent.deletePath(gamelistXml);
        Files.createDirectories(gamelistXml.getParent());
        writeGameList(platformContext, gamelistXml);
        fileComponent.copyPath(PathPair.builder()
                                       .sourcePath(gamelistXml)
                                       .targetPath(PathUtils.ESDE_PLATFORM_GAMELIST.get(platformContext))
                                       .build());
    }

    private void writeGameList(PlatformContext platformContext, Path gamelistXml) throws Exception {
        if (platformContext.getMatchResults() == null || platformContext.getMatchResults().isEmpty()) {
            throw new IllegalStateException("matchResults is empty, run FileContextToSSGamePackageMatchHandler before GameListHandler");
        }

        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        var gameList = document.createElement("gameList");
        document.appendChild(gameList);

        for (var matchResult : platformContext.getMatchResults()) {
            appendGames(platformContext, document, gameList, matchResult);
        }

        var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(document), new StreamResult(gamelistXml.toFile()));
    }

    private void appendGames(PlatformContext platformContext,
                             Document document,
                             Element gameList,
                             MatchResult matchResult) {
        if (matchResult.getFileContextByArea() == null || matchResult.getFileContextByArea().isEmpty()) {
            return;
        }
        if (matchResult.getRenameResultByArea() == null || matchResult.getRenameResultByArea().isEmpty()) {
            throw new IllegalStateException("renameResultByArea is empty, run RenameHandler before GameListHandler");
        }

        for (var entry : matchResult.getFileContextByArea().entrySet()) {
            var area = entry.getKey();
            var fileContext = entry.getValue();
            var renameResult = matchResult.getRenameResultByArea().get(area);
            if (renameResult == null) {
                throw new IllegalStateException("Rename result not found for area: " + area);
            }

            var finalRomName = renameResult.get(fileContext.getFileName());
            if (finalRomName == null || finalRomName.isBlank()) {
                throw new IllegalStateException("Rename result not found for area=%s, file=%s"
                        .formatted(area, fileContext.getFileName()));
            }

            var ssGamePackage = requireSSGamePackage(matchResult, area);
            appendGame(platformContext, document, gameList, area, finalRomName, fileContext.getExtension(), ssGamePackage);
        }
    }

    private void appendGame(PlatformContext platformContext,
                            Document document,
                            Element gameList,
                            String area,
                            String finalRomName,
                            String extension,
                            SSGamePackage ssGamePackage) {
        var game = document.createElement("game");
        gameList.appendChild(game);

        appendTextElement(document, game, "path",
                "./" + PathUtils.esdeAreaDirectoryName(platformContext, area) + "/" + finalRomName + extension);
        appendTextElement(document, game, "name", finalRomName);
        appendTextElement(document, game, "desc", ssGamePackage.getDescription());
        appendTextElement(document, game, "rating", "0");
        appendTextElement(document, game, "releasedate", formatReleaseDate(getReleaseDate(ssGamePackage, area)));
        appendTextElement(document, game, "developer", ssGamePackage.getDeveloper());
        appendTextElement(document, game, "publisher", ssGamePackage.getPublisher());
        appendTextElement(document, game, "genre", ssGamePackage.getGenre());
        appendTextElement(document, game, "players", ssGamePackage.getPlayer());
    }

    private String getReleaseDate(SSGamePackage ssGamePackage, String area) {
        if (ssGamePackage.getReleaseDateByArea() == null) {
            throw new IllegalStateException("SSGamePackage.releaseDateByArea is empty: ssPackageId=%s, area=%s"
                    .formatted(ssGamePackage.getId(), area));
        }
        var releaseDate = ssGamePackage.getReleaseDateByArea().get(area);
        if (releaseDate == null || releaseDate.isBlank()) {
            throw new IllegalStateException("SSGamePackage.releaseDateByArea missing: ssPackageId=%s, area=%s"
                    .formatted(ssGamePackage.getId(), area));
        }
        return releaseDate;
    }

    private SSGamePackage requireSSGamePackage(MatchResult matchResult, String area) {
        var ssGamePackageByArea = matchResult.getSsGamePackageByArea();
        if (ssGamePackageByArea == null || ssGamePackageByArea.isEmpty()) {
            throw new IllegalStateException("SSGamePackage not found for area: " + area);
        }

        var ssGamePackage = ssGamePackageByArea.get(area);
        if (ssGamePackage == null) {
            ssGamePackage = ssGamePackageByArea.values().stream()
                    .filter(candidate -> candidate != null
                            && candidate.getSsGameByArea() != null
                            && candidate.getSsGameByArea().containsKey(area))
                    .findFirst()
                    .orElse(null);
        }
        if (ssGamePackage == null) {
            throw new IllegalStateException("SSGamePackage not found for area: " + area);
        }
        return ssGamePackage;
    }

    private void appendTextElement(Document document, Element parent, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        var element = document.createElement(name);
        element.appendChild(document.createTextNode(value));
        parent.appendChild(element);
    }

    private String formatReleaseDate(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank()) {
            return null;
        }

        var digits = releaseDate.replaceAll("\\D", "");
        if (digits.length() >= 8) {
            return digits.substring(0, 8) + "T000000";
        }
        if (digits.length() == 6) {
            return digits + "01T000000";
        }
        if (digits.length() == 4) {
            return digits + "0101T000000";
        }
        return null;
    }
}

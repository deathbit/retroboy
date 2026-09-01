package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Component
public class GameListHandler {

    @Autowired
    private FileComponent fileComponent;

    public void handle(PlatformContext platformContext) {
        var targetPath = PathUtils.ESDE_PLATFORM_GAMELIST.get(platformContext);
        fileComponent.copyPath(PathPair.builder().sourcePath(PathUtils.PLATFORM_GAMELIST_XML.get(platformContext))
                                       .targetPath(targetPath).build());
        var gameListPath = PathUtils.ESDE_PLATFORM_GAMELIST_XML.get(platformContext);
        validateGameDescriptions(gameListPath);
        validateUniqueGameNames(gameListPath);
        updateGameNames(gameListPath);
    }

    private void validateGameDescriptions(Path gameListPath) {
        var document = parseGameList(gameListPath);
        var gameNodes = document.getElementsByTagName("game");
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var gameElement = (Element) gameNodes.item(i);
            var gamePath = getGamePath(gameElement);
            var descriptionNodes = gameElement.getElementsByTagName("desc");
            if (descriptionNodes.getLength() == 0 || descriptionNodes.item(0).getTextContent().isBlank()) {
                throw new IllegalArgumentException("gamelist.xml 中游戏缺少 desc: path=" + gamePath);
            }
        }
    }

    private void validateUniqueGameNames(Path gameListPath) {
        var document = parseGameList(gameListPath);
        var gameNodes = document.getElementsByTagName("game");
        var gamePathByAreaAndName = new HashMap<String, Map<String, String>>();
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var gameElement = (Element) gameNodes.item(i);
            var gamePath = getGamePath(gameElement);
            validateUniqueGameName(gamePathByAreaAndName, gamePath, fileNameWithoutExtension(gamePath));
        }
    }

    private void updateGameNames(Path gameListPath) {
        var document = parseGameList(gameListPath);
        var gameNodes = document.getElementsByTagName("game");
        for (int i = 0; i < gameNodes.getLength(); i++) {
            var gameElement = (Element) gameNodes.item(i);
            var gameName = fileNameWithoutExtension(getGamePath(gameElement));
            var nameNodes = gameElement.getElementsByTagName("name");
            if (nameNodes.getLength() > 0) {
                nameNodes.item(0).setTextContent(gameName);
            } else {
                var nameElement = document.createElement("name");
                nameElement.setTextContent(gameName);
                gameElement.appendChild(nameElement);
            }
        }

        try {
            removeBlankTextNodes(document);
            var transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(gameListPath.toFile()));
        } catch (TransformerException e) {
            throw new RuntimeException("Failed to write gamelist.xml: " + gameListPath, e);
        }
    }

    private Document parseGameList(Path gameListPath) {
        try {
            var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return documentBuilderFactory.newDocumentBuilder().parse(gameListPath.toFile());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Failed to read gamelist.xml: " + gameListPath, e);
        }
    }

    private String getGamePath(Element gameElement) {
        var pathNodes = gameElement.getElementsByTagName("path");
        if (pathNodes.getLength() == 0 || pathNodes.item(0).getTextContent().isBlank()) {
            throw new IllegalArgumentException("gamelist.xml 中存在缺少 path 的 game 节点");
        }
        return pathNodes.item(0).getTextContent().trim();
    }

    private void validateUniqueGameName(Map<String, Map<String, String>> gamePathByAreaAndName,
                                        String gamePath,
                                        String gameName) {
        var area = extractArea(gamePath);
        var gamePathByName = gamePathByAreaAndName.computeIfAbsent(area, ignored -> new HashMap<>());
        var existingGamePath = gamePathByName.putIfAbsent(gameName, gamePath);
        if (existingGamePath != null) {
            throw new IllegalArgumentException(
                    "gamelist.xml 中存在重复游戏名称: area=%s, name=%s, path1=%s, path2=%s"
                            .formatted(area, gameName, existingGamePath, gamePath));
        }
    }

    private String extractArea(String gamePath) {
        var normalizedPath = gamePath.replace('\\', '/');
        var fileNameSeparator = normalizedPath.lastIndexOf('/');
        if (fileNameSeparator <= 0) {
            throw new IllegalArgumentException("gamelist.xml 中游戏 path 缺少地区目录: path=" + gamePath);
        }
        var directoryPath = normalizedPath.substring(0, fileNameSeparator);
        var directoryName = directoryPath.substring(directoryPath.lastIndexOf('/') + 1);
        var areaSeparator = directoryName.lastIndexOf(" - ");
        if (areaSeparator == -1 || areaSeparator + 3 == directoryName.length()) {
            throw new IllegalArgumentException("gamelist.xml 中游戏 path 地区目录格式错误: path=" + gamePath);
        }
        return directoryName.substring(areaSeparator + 3);
    }

    private String fileNameWithoutExtension(String path) {
        var normalizedPath = path.trim().replace('\\', '/');
        var fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        var dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private void removeBlankTextNodes(Node node) {
        var child = node.getFirstChild();
        while (child != null) {
            var next = child.getNextSibling();
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().isBlank()) {
                node.removeChild(child);
            } else {
                removeBlankTextNodes(child);
            }
            child = next;
        }
    }
}

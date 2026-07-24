package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.PathPair;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.platform.GameListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Component
public class GameListHandlerImpl implements GameListHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) {
        String targetPath = String.format("%s\\ES-DE\\gamelists\\%s",
            ruleContext.getGlobalConfig().getEsdeHomePath(), ruleContext.getPlatformName());
        fileComponent.copyPath(PathPair.builder().sourcePath(String.format("%s\\platform\\%s\\gamelists\\%s\\gamelist.xml",
                                           ruleContext.getGlobalConfig().getResourcesHomePath(),
                                           ruleContext.getPlatformName(),
                                           ruleContext.getPlatformName()))
                                       .targetPath(targetPath).build());
        updateGameNames(targetPath + "\\gamelist.xml");
    }

    private void updateGameNames(String gameListPath) {
        try {
            var gameListFile = new File(gameListPath);
            var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var document = documentBuilderFactory.newDocumentBuilder().parse(gameListFile);
            var gameNodes = document.getElementsByTagName("game");
            for (int i = 0; i < gameNodes.getLength(); i++) {
                var gameElement = (Element) gameNodes.item(i);
                var pathNodes = gameElement.getElementsByTagName("path");
                if (pathNodes.getLength() == 0 || pathNodes.item(0).getTextContent().isBlank()) {
                    throw new IllegalArgumentException("gamelist.xml 中存在缺少 path 的 game 节点");
                }

                var gameName = fileNameWithoutExtension(pathNodes.item(0).getTextContent());
                var nameNodes = gameElement.getElementsByTagName("name");
                if (nameNodes.getLength() > 0) {
                    nameNodes.item(0).setTextContent(gameName);
                } else {
                    var nameElement = document.createElement("name");
                    nameElement.setTextContent(gameName);
                    gameElement.appendChild(nameElement);
                }
            }

            removeBlankTextNodes(document);
            var transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(gameListFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String fileNameWithoutExtension(String path) {
        var normalizedPath = path.trim().replace('\\', '/');
        var fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
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

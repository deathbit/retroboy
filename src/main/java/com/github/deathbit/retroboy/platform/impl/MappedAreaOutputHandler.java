package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.gamepackage.NoIntroGamePackage;
import com.github.deathbit.retroboy.domain.gamepackage.WikiGamePackage;
import com.github.deathbit.retroboy.enums.MediaAssetType;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Component
public class MappedAreaOutputHandler {

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    public void handle(PlatformContext platformContext) throws Exception {
        var areaMapping = buildAreaMapping(platformContext);
        var directoryMoves = buildDirectoryMoves(platformContext, areaMapping);
        moveMappedAreaDirectories(directoryMoves);
        updateGamelistPaths(PathUtils.ESDE_PLATFORM_GAMELIST_XML.get(platformContext), platformContext, areaMapping);
    }

    private List<DirectoryMove> buildDirectoryMoves(PlatformContext platformContext, Map<String, String> areaMapping) {
        var directoryMoves = new ArrayList<DirectoryMove>();
        for (var entry : areaMapping.entrySet()) {
            var sourceArea = entry.getKey();
            var targetArea = outputArea(platformContext, entry.getValue());
            if (sourceArea.equals(targetArea)) {
                continue;
            }

            var sourceAreaDirectoryName = PathUtils.esdeAreaDirectoryName(platformContext, sourceArea);
            var targetAreaDirectoryName = PathUtils.esdeAreaDirectoryName(platformContext, targetArea);
            directoryMoves.add(new DirectoryMove(
                PathUtils.ESDE_PLATFORM_ROMS.get(platformContext).resolve(sourceAreaDirectoryName),
                PathUtils.ESDE_PLATFORM_ROMS.get(platformContext).resolve(targetAreaDirectoryName)
            ));
            for (var mediaAssetType : MediaAssetType.values()) {
                var mediaRoot = PathUtils.ESDE_PLATFORM_MEDIA.get(platformContext).resolve(mediaAssetType.getDirectoryName());
                directoryMoves.add(new DirectoryMove(
                    mediaRoot.resolve(sourceAreaDirectoryName),
                    mediaRoot.resolve(targetAreaDirectoryName)
                ));
            }
        }
        return directoryMoves;
    }

    private void moveMappedAreaDirectories(List<DirectoryMove> directoryMoves) throws Exception {
        var sourceDirectories = new ArrayList<Path>();
        var fileMoves = buildFileMoves(directoryMoves, sourceDirectories);

        for (var fileMove : fileMoves) {
            Files.createDirectories(fileMove.targetPath().getParent());
            Files.move(fileMove.sourcePath(), fileMove.targetPath());
        }
        deleteSourceDirectories(sourceDirectories);
    }

    private List<FileMove> buildFileMoves(List<DirectoryMove> directoryMoves, List<Path> sourceDirectories) throws Exception {
        var fileMoves = new ArrayList<FileMove>();
        var plannedTargets = new LinkedHashMap<Path, Path>();
        for (var directoryMove : directoryMoves) {
            var sourceDirectory = directoryMove.sourceDirectory().toAbsolutePath().normalize();
            var targetDirectory = directoryMove.targetDirectory().toAbsolutePath().normalize();
            if (sourceDirectory.equals(targetDirectory) || Files.notExists(sourceDirectory)) {
                continue;
            }
            if (!Files.isDirectory(sourceDirectory)) {
                throw new IllegalStateException("Mapped area source is not a directory: " + sourceDirectory);
            }

            sourceDirectories.add(sourceDirectory);
            try (Stream<Path> paths = Files.walk(sourceDirectory)) {
                for (var sourcePath : paths.filter(Files::isRegularFile).toList()) {
                    var targetPath = targetDirectory.resolve(sourceDirectory.relativize(sourcePath)).toAbsolutePath().normalize();
                    if (Files.exists(targetPath)) {
                        throw new IllegalStateException("Mapped area move target already exists: source=%s, target=%s"
                            .formatted(sourcePath, targetPath));
                    }

                    var previousSource = plannedTargets.putIfAbsent(targetPath, sourcePath);
                    if (previousSource != null) {
                        throw new IllegalStateException("Mapped area move target conflict: target=%s, source1=%s, source2=%s"
                            .formatted(targetPath, previousSource, sourcePath));
                    }
                    fileMoves.add(new FileMove(sourcePath, targetPath));
                }
            }
        }
        return fileMoves;
    }

    private void deleteSourceDirectories(List<Path> sourceDirectories) throws Exception {
        for (var sourceDirectory : sourceDirectories) {
            if (Files.notExists(sourceDirectory)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(sourceDirectory)) {
                for (var path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private void updateGamelistPaths(Path gamelistXml, PlatformContext platformContext, Map<String, String> areaMapping) throws Exception {
        if (Files.notExists(gamelistXml)) {
            throw new IllegalStateException("ES-DE gamelist.xml not found: " + gamelistXml);
        }

        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(gamelistXml.toFile());
        var pathNodes = document.getElementsByTagName("path");
        var modified = false;
        for (int i = 0; i < pathNodes.getLength(); i++) {
            var pathNode = (Element) pathNodes.item(i);
            var path = pathNode.getTextContent();
            var mappedPath = mapGamePath(platformContext, areaMapping, path);
            if (!path.equals(mappedPath)) {
                pathNode.setTextContent(mappedPath);
                modified = true;
            }
        }
        if (!modified) {
            return;
        }

        var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(document), new StreamResult(gamelistXml.toFile()));
    }

    private String mapGamePath(PlatformContext platformContext, Map<String, String> areaMapping, String gamePath) {
        for (var entry : areaMapping.entrySet()) {
            var sourceArea = entry.getKey();
            var targetArea = outputArea(platformContext, entry.getValue());
            if (sourceArea.equals(targetArea)) {
                continue;
            }

            var sourcePrefix = "./" + PathUtils.esdeAreaDirectoryName(platformContext, sourceArea) + "/";
            if (gamePath.startsWith(sourcePrefix)) {
                return "./" + PathUtils.esdeAreaDirectoryName(platformContext, targetArea) + "/"
                    + gamePath.substring(sourcePrefix.length());
            }
        }
        return gamePath;
    }

    private String outputArea(PlatformContext platformContext, String area) {
        if (platformContext.getPlatform() == Platform.NES && "PAL".equals(area)) {
            return "EUR";
        }
        return area;
    }

    private Map<String, String> buildAreaMapping(PlatformContext platformContext) {
        return getPlatformProcessor(platformContext).gameDBToWikiDBAreaMapping(
            buildGameAreas(platformContext.getNoIntroGamePackages()),
            buildWikiAreas(platformContext.getWikiGamePackages())
        );
    }

    private List<String> buildGameAreas(List<NoIntroGamePackage> noIntroGamePackages) {
        var areas = new LinkedHashSet<String>();
        for (var pkg : noIntroGamePackages == null ? List.<NoIntroGamePackage>of() : noIntroGamePackages) {
            areas.addAll(pkg.getNoIntroGameByArea().keySet());
        }
        return new ArrayList<>(areas);
    }

    private List<String> buildWikiAreas(List<WikiGamePackage> wikiGamePackages) {
        var areas = new LinkedHashSet<String>();
        for (var pkg : wikiGamePackages == null ? List.<WikiGamePackage>of() : wikiGamePackages) {
            areas.addAll(pkg.getWikiGameByArea().keySet());
        }
        return new ArrayList<>(areas);
    }

    private PlatformProcessor getPlatformProcessor(PlatformContext platformContext) {
        var platformProcessor = platformProcessorMap.get(platformContext.getPlatform());
        if (platformProcessor == null) {
            throw new IllegalStateException("PlatformProcessor not found for platform: " + platformContext.getPlatform());
        }
        return platformProcessor;
    }

    private record DirectoryMove(Path sourceDirectory, Path targetDirectory) {
    }

    private record FileMove(Path sourcePath, Path targetPath) {
    }
}

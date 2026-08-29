//package com.github.deathbit.retroboy.platform.impl;
//
//import com.github.deathbit.retroboy.component.FileComponent;
//import com.github.deathbit.retroboy.domain.AreaRenameResult;
//import com.github.deathbit.retroboy.domain.FileContext;
//import com.github.deathbit.retroboy.domain.ProgressBar;
//import com.github.deathbit.retroboy.domain.PlatformContext;
//import com.github.deathbit.retroboy.enums.Area;
//import com.github.deathbit.retroboy.util.PathUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Component
//public class RenameGameHandler {
//
//    @Autowired
//    private FileComponent fileComponent;
//    public void handle(PlatformContext platformContext) throws Exception {
//        platformContext.getAreaPassMap().forEach((area, roms) -> {
//            var areaRenameResults = platformContext.getAreaRenameResultMap().computeIfAbsent(area, ignored -> new LinkedHashMap<>());
//            ProgressBar pb = new ProgressBar("命名游戏");
//            pb.startTask(roms.size());
//            for (int i = 0; i < roms.size(); i++) {
//                var oldName = roms.get(i);
//                var newName = buildNewName(oldName, platformContext);
//                areaRenameResults.put(oldName, AreaRenameResult.builder()
//                                                               .oldName(oldName)
//                                                               .newName(newName)
//                                                               .finalName(removeExtension(newName))
//                                                               .renamed(!oldName.equals(newName))
//                                                               .build());
//                fileComponent.rename(PathUtils.esdeAreaRom(platformContext, area, oldName), newName);
//                pb.updateTask(i);
//            }
//            pb.finishTaskAndClose();
//        });
//        writeRomWiki(platformContext);
//    }
//
//    private void writeRomWiki(PlatformContext platformContext) throws Exception {
//        var wikiPath = PathUtils.PLATFORM_ROM_WIKI.get(platformContext);
//        var content = new StringBuilder();
//        for (var area : Area.values()) {
//            var finalNames = platformContext.getAreaRenameResultMap()
//                                            .getOrDefault(area, Map.of())
//                                            .values()
//                                            .stream()
//                                            .map(AreaRenameResult::getFinalName)
//                                            .filter(finalName -> finalName != null && !finalName.isBlank())
//                                            .sorted(Comparator.naturalOrder())
//                                            .toList();
//            content.append(area.name())
//                    .append("(")
//                    .append(finalNames.size())
//                    .append("):")
//                    .append(System.lineSeparator());
//            finalNames.forEach(finalName -> content.append(finalName).append(System.lineSeparator()));
//            content.append(System.lineSeparator());
//        }
//        Files.createDirectories(wikiPath.getParent());
//        Files.writeString(wikiPath, content.toString(), StandardCharsets.UTF_8);
//    }
//
//    private String buildNewName(String oldName, PlatformContext platformContext) {
//        FileContext fileContext = platformContext.getFileContextMap().get(oldName);
//        if (platformContext.getRenameOptionMap().containsKey(fileContext.getFileName())) {
//            return platformContext.getRenameOptionMap().get(fileContext.getFileName());
//        }
//        return normalizeLeadingArticle(fileContext.getNamePart()) + fileContext.getExtension();
//    }
//
//    private String removeExtension(String fileName) {
//        var dotIndex = fileName.lastIndexOf('.');
//        if (dotIndex <= 0) {
//            return fileName;
//        }
//        return fileName.substring(0, dotIndex);
//    }
//
//    private String normalizeLeadingArticle(String namePart) {
//        return normalizeTrailingArticle(normalizeTrailingArticle(namePart, "The"), "A");
//    }
//
//    private String normalizeTrailingArticle(String namePart, String article) {
//        var suffix = ", " + article;
//        var separatorIndex = namePart.indexOf(" - ");
//        if (separatorIndex == -1) {
//            if (namePart.endsWith(suffix)) {
//                return article + " " + namePart.substring(0, namePart.length() - suffix.length());
//            }
//            return namePart;
//        }
//
//        var title = namePart.substring(0, separatorIndex);
//        if (!title.endsWith(suffix)) {
//            return namePart;
//        }
//
//        return article + " " + title.substring(0, title.length() - suffix.length()) + namePart.substring(separatorIndex);
//    }
//}

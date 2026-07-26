package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.AreaRenameResult;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.handler.platform.RenameGameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RenameGameHandlerImpl implements RenameGameHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        ruleContext.getAreaPassMap().forEach((area, roms) -> {
            var areaRenameResults = ruleContext.getAreaRenameResultMap().computeIfAbsent(area, ignored -> new LinkedHashMap<>());
            ProgressBar pb = new ProgressBar("命名游戏");
            pb.startTask(roms.size());
            for (int i = 0; i < roms.size(); i++) {
                var oldName = roms.get(i);
                var newName = buildNewName(oldName, ruleContext);
                areaRenameResults.put(oldName, AreaRenameResult.builder()
                                                               .oldName(oldName)
                                                               .newName(newName)
                                                               .finalName(removeExtension(newName))
                                                               .renamed(!oldName.equals(newName))
                                                               .build());
                fileComponent.rename(String.format("%s\\ROMs\\%s\\%s-%s\\%s",
                    ruleContext.getGlobalConfig().getEsdeHomePath(),
                    ruleContext.getPlatformName(),
                    ruleContext.getPlatform().name(),
                    area.name(), oldName), newName);
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        });
        writeRomWiki(ruleContext);
    }

    private void writeRomWiki(RuleContext ruleContext) throws Exception {
        var wikiPath = Path.of(ruleContext.getGlobalConfig().getResourcesHomePath(),
                "platform",
                ruleContext.getPlatformName(),
                "wiki",
                ruleContext.getPlatform().name() + "-ROM.txt");
        var content = new StringBuilder();
        for (var area : Area.values()) {
            var finalNames = ruleContext.getAreaRenameResultMap()
                    .getOrDefault(area, Map.of())
                    .values()
                    .stream()
                    .map(AreaRenameResult::getFinalName)
                    .filter(finalName -> finalName != null && !finalName.isBlank())
                    .sorted(Comparator.naturalOrder())
                    .toList();
            content.append(area.name())
                    .append("(")
                    .append(finalNames.size())
                    .append("):")
                    .append(System.lineSeparator());
            finalNames.forEach(finalName -> content.append(finalName).append(System.lineSeparator()));
            content.append(System.lineSeparator());
        }
        Files.createDirectories(wikiPath.getParent());
        Files.writeString(wikiPath, content.toString(), StandardCharsets.UTF_8);
    }

    private String buildNewName(String oldName, RuleContext ruleContext) {
        FileContext fileContext = ruleContext.getFileContextMap().get(oldName);
        if (ruleContext.getRenameOptionMap().containsKey(fileContext.getFileName())) {
            return ruleContext.getRenameOptionMap().get(fileContext.getFileName());
        }
        return normalizeLeadingArticle(fileContext.getNamePart()) + fileContext.getExtension();
    }

    private String removeExtension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private String normalizeLeadingArticle(String namePart) {
        return normalizeTrailingArticle(normalizeTrailingArticle(namePart, "The"), "A");
    }

    private String normalizeTrailingArticle(String namePart, String article) {
        var suffix = ", " + article;
        var separatorIndex = namePart.indexOf(" - ");
        if (separatorIndex == -1) {
            if (namePart.endsWith(suffix)) {
                return article + " " + namePart.substring(0, namePart.length() - suffix.length());
            }
            return namePart;
        }

        var title = namePart.substring(0, separatorIndex);
        if (!title.endsWith(suffix)) {
            return namePart;
        }

        return article + " " + title.substring(0, title.length() - suffix.length()) + namePart.substring(separatorIndex);
    }
}

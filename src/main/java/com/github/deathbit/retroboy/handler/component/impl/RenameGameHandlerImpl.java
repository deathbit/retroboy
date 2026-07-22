package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.RenameGameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RenameGameHandlerImpl implements RenameGameHandler {

    @Autowired
    private FileComponent fileComponent;

    @Override
    public void handle(RuleContext ruleContext) throws Exception {
        ruleContext.getAreaPassMap().forEach((area, roms) -> {
            ProgressBar pb = new ProgressBar("命名游戏");
            pb.startTask(roms.size());
            for (int i = 0; i < roms.size(); i++) {
                var oldName = roms.get(i);
                fileComponent.rename(String.format("%s\\ROMs\\%s\\%s-%s\\%s",
                        ruleContext.getGlobalConfig().getEsdeHomePath(),
                        ruleContext.getPlatformName(),
                        ruleContext.getPlatform().name(),
                        area.name(), oldName), buildNewName(oldName, ruleContext));
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        });
    }

    private String buildNewName(String oldName, RuleContext ruleContext) {
        FileContext fileContext = ruleContext.getFileContextMap().get(oldName);
        if (ruleContext.getRenameOptionMap().containsKey(fileContext.getFileName())) {
            return ruleContext.getRenameOptionMap().get(fileContext.getFileName());
        }
        return normalizeLeadingArticle(fileContext.getNamePart()) + fileContext.getExtension();
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


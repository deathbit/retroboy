package com.github.deathbit.retroboy.handler;

import java.util.ArrayList;
import java.util.List;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.RuleConfig;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

public abstract class AbstractHandler implements Handler {

    @Override
    public RuleContext buildRuleContext(RuleConfig ruleConfig, AppConfig appConfig) {
        return null;
    }

    @Override
    public FileContext buildFileContext(String fileName) {
        // Extract full name (without extension)
        String fullName = fileName;
        if (fileName.contains(".")) {
            fullName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        // Parse name part and tag part
        String namePart = fullName;
        String tagPart = "";
        List<String> tags = new ArrayList<>();

        int firstParen = fullName.indexOf('(');
        if (firstParen != -1) {
            namePart = fullName.substring(0, firstParen).trim();
            tagPart = fullName.substring(firstParen);

            // Extract individual tags
            int start = 0;
            while ((start = tagPart.indexOf('(', start)) != -1) {
                int end = tagPart.indexOf(')', start);
                if (end != -1) {
                    tags.add(tagPart.substring(start + 1, end));
                    start = end + 1;
                } else {
                    break;
                }
            }
        }

        return FileContext.builder()
            .fileName(fileName)
            .fullName(fullName)
            .namePart(namePart)
            .tagPart(tagPart)
            .tags(tags)
            .build();
    }

    @Override
    public Rule buildJapanRule() {
        return Rules.IS_JAPAN_BASE;
    }

    @Override
    public Rule buildUsaRule() {
        return Rules.IS_USA_BASE;
    }

    @Override
    public Rule buildEuropeRule() {
        return Rules.IS_EUROPE_BASE;
    }

    @Override
    public void handle() {

    }
}

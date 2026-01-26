package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.HandlerInput;
import com.github.deathbit.retroboy.domain.RuleConfig;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractHandler implements Handler {

    @Override
    public void handle(HandlerInput handlerInput) {






















    }

    private RuleContext buildRuleContext(HandlerInput handlerInput) {
        Set<String> licensed = new HashSet<>();

        try {
            File datFile = new File(ruleConfig.getDatFile());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(datFile);

            // Get all game elements
            NodeList gameNodes = document.getElementsByTagName("game");

            // Extract name attribute from each game element
            for (int i = 0; i < gameNodes.getLength(); i++) {
                Element gameElement = (Element) gameNodes.item(i);
                String name = gameElement.getAttribute("name");
                if (!name.isEmpty()) {
                    licensed.add(name);
                }
            }
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e) {
            throw new RuntimeException("Failed to parse DAT file: " + ruleConfig.getDatFile(), e);
        }

        return RuleContext.builder()
                .licensed(licensed)
                .globalTagBlackList(appConfig.getGlobalTagBlackList())
                .build();

    }

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
}

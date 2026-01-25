package com.github.deathbit.retroboy.handler.handlers;

import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.handler.Handler;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class NesHandler implements Handler {

    @Override
    public RuleContext buildRuleContext(RuleConfig ruleConfig) {
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
                .build();
    }

    @Override
    public FileContext buildFileContext(String fileName) {
        return null;
    }

    @Override
    public List<Rule> buildJapanRuleChain() {
        return List.of(
                Rules.IS_JAPAN_LICENSED
        );
    }

    @Override
    public List<Rule> buildUSARuleChain() {
        return List.of(
                Rules.IS_USA_LICENSED
        );
    }

    @Override
    public List<Rule> buildEuropeRuleChain() {
        return List.of(
                Rules.IS_EUROPE_LICENSED
        );
    }

    @Override
    public void handle() {

    }
}

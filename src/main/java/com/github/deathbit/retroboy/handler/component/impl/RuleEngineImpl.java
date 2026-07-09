package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.RuleContextInitializer;
import com.github.deathbit.retroboy.handler.component.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleEngineImpl implements RuleEngine {

    @Autowired
    private RuleContextInitializer ruleContextInitializer;

    @Override
    public void handle(RuleContext ruleContext) {
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var area = areaConfig.getArea();
            var rule = ruleContext.getRuleMap().get(area);
            ruleContextInitializer.initializeArea(ruleContext, area);

            for (var entry : ruleContext.getFileContextMap().entrySet()) {
                var fileName = entry.getKey();
                var fileContext = entry.getValue();
                ruleContextInitializer.initializeFile(ruleContext, fileContext);
                if (rule.pass(ruleContext, fileContext)) {
                    ruleContext.getAreaFinalMap().get(area).add(fileName);
                } else {
                    ruleContext.getAreaFailureReportMap().get(area).add(String.format("%s - %s",
                        fileName, String.join(", ", ruleContext.getFailureReasons())));
                }
            }
        }
    }
}


package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.SelectAreaFilesHandlerComponent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SelectAreaFilesHandlerComponentImpl implements SelectAreaFilesHandlerComponent {

    @Override
    public void handle(RuleContext ruleContext) {
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var area = areaConfig.getArea();
            ruleContext.setCurrentArea(area);
            ruleContext.setCurrentAreaConfig(areaConfig);
            var rule = ruleContext.getRuleMap().get(area);

            for (var entry : ruleContext.getFileContextMap().entrySet()) {
                var fileName = entry.getKey();
                var fileContext = entry.getValue();
                var failureReasons = new ArrayList<String>();
                ruleContext.setFailureReasons(failureReasons);
                if (rule.pass(ruleContext, fileContext)) {
                    ruleContext.getAreaFinalMap().get(area).add(fileName);
                } else {
                    ruleContext.getAreaFailureReportMap().get(area).add(failureReportLine(fileName, String.join(", ", failureReasons)));
                }
            }
        }
        ruleContext.setCurrentArea(null);
        ruleContext.setCurrentAreaConfig(null);
    }

    private String failureReportLine(String fileName, String failureReason) {
        return fileName + " - " + failureReason;
    }
}


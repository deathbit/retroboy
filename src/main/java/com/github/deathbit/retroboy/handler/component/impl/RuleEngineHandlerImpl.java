package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.component.RuleEngineHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RuleEngineHandlerImpl implements RuleEngineHandler {

    @Override
    public void handle(RuleContext ruleContext) {
        for (var areaConfig : ruleContext.getPlatformPackTaskConfig().getAreaConfigs()) {
            var area = areaConfig.getArea();
            var rule = ruleContext.getRuleMap().get(area);
            ruleContext.setCurrentAreaConfig(areaConfig);
            ProgressBar pb = new ProgressBar("解析规则");
            pb.startTask(ruleContext.getFileContextMap().size());
            for (int i = 0; i < ruleContext.getFileContextMap().size(); i++) {
                var entry = ruleContext.getFileContextMap().entrySet().stream().toList().get(i);
                var fileName = entry.getKey();
                var fileContext = entry.getValue();
                ruleContext.setRomNotPassReasons(new ArrayList<>());
                if (rule.pass(ruleContext, fileContext)) {
                    ruleContext.getAreaPassMap().computeIfAbsent(area, ignored -> new ArrayList<>()).add(fileName);
                } else {
                    ruleContext.getAreaNotPassReportMap()
                            .computeIfAbsent(area, ignored -> new ArrayList<>())
                            .add(String.format("%s - %s", fileName, String.join(", ", ruleContext.getRomNotPassReasons())));
                }
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        }
    }
}

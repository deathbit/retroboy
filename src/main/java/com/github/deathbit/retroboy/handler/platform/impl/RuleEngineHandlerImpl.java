package com.github.deathbit.retroboy.handler.platform.impl;

import com.github.deathbit.retroboy.domain.AreaRuleResult;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.platform.RuleEngineHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Component
public class RuleEngineHandlerImpl implements RuleEngineHandler {

    @Override
    public void handle(RuleContext ruleContext) {
        for (var areaConfig : ruleContext.getPlatformPackTaskConfig().getAreaConfigs()) {
            var area = areaConfig.getArea();
            var rule = ruleContext.getRuleMap().get(area);
            ruleContext.setCurrentAreaConfig(areaConfig);
            var areaRuleResults = ruleContext.getAreaRuleResultMap().computeIfAbsent(area, ignored -> new LinkedHashMap<>());
            var fileContextEntries = ruleContext.getFileContextMap().entrySet().stream().toList();
            ProgressBar pb = new ProgressBar("解析规则");
            pb.startTask(fileContextEntries.size());
            for (int i = 0; i < fileContextEntries.size(); i++) {
                var entry = fileContextEntries.get(i);
                var fileName = entry.getKey();
                var fileContext = entry.getValue();
                ruleContext.setRomNotPassReasons(new ArrayList<>());
                var passed = rule.pass(ruleContext, fileContext);
                var reasons = passed ? new ArrayList<String>() : new ArrayList<>(ruleContext.getRomNotPassReasons());
                areaRuleResults.put(fileName, AreaRuleResult.builder()
                                                            .fileName(fileName)
                                                            .passed(passed)
                                                            .reasons(reasons)
                                                            .build());
                if (passed) {
                    ruleContext.getAreaPassMap().computeIfAbsent(area, ignored -> new ArrayList<>()).add(fileName);
                }
                pb.updateTask(i);
            }
            pb.finishTaskAndClose();
        }
    }
}

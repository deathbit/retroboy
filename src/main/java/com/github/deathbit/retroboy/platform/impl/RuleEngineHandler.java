package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.RuleContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuleEngineHandler {
    public void handle(RuleContext ruleContext) {
        List<GameDB> gameDBS = ruleContext.getGameDBList().stream()
                .filter(gameDB -> gameDB.getLicensed().isEmpty())
                .filter(gameDB -> gameDB.getBios().isEmpty())
                .filter(gameDB -> gameDB.getDevstatus().isEmpty())
                .filter(gameDB -> gameDB.getPhysical().isEmpty())
                .filter(gameDB -> gameDB.getRegparent().contains("PARENT"))
                .filter(gameDB -> gameDB.getSpecial1().isEmpty())
                .filter(gameDB -> gameDB.getSpecial2().isEmpty())
                .filter(gameDB -> gameDB.getRegparent().contains("JPN PARENT"))
                .toList();

        gameDBS.forEach(gameDB -> {
            System.out.println(gameDB.getRomName());
        });

        System.out.println();









//        for (var areaConfig : ruleContext.getPlatformPackTaskConfig().getAreaConfigs()) {
//            var area = areaConfig.getArea();
//            var rule = ruleContext.getRuleMap().get(area);
//            ruleContext.setCurrentAreaConfig(areaConfig);
//            var areaRuleResults = ruleContext.getAreaRuleResultMap().computeIfAbsent(area, ignored -> new LinkedHashMap<>());
//            var fileContextEntries = ruleContext.getFileContextMap().entrySet().stream().toList();
//            ProgressBar pb = new ProgressBar("解析规则");
//            pb.startTask(fileContextEntries.size());
//            for (int i = 0; i < fileContextEntries.size(); i++) {
//                var entry = fileContextEntries.get(i);
//                var fileName = entry.getKey();
//                var fileContext = entry.getValue();
//                ruleContext.setRomNotPassReasons(new ArrayList<>());
//                var passed = rule.pass(ruleContext, fileContext);
//                var reasons = passed ? new ArrayList<String>() : new ArrayList<>(ruleContext.getRomNotPassReasons());
//                areaRuleResults.put(fileName, AreaRuleResult.builder()
//                                                            .fileName(fileName)
//                                                            .passed(passed)
//                                                            .reasons(reasons)
//                                                            .build());
//                if (passed) {
//                    ruleContext.getAreaPassMap().computeIfAbsent(area, ignored -> new ArrayList<>()).add(fileName);
//                }
//                pb.updateTask(i);
//            }
//            pb.finishTaskAndClose();
//        }
    }
}

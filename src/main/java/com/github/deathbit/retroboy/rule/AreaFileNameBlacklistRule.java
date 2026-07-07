package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

import java.util.Optional;

public class AreaFileNameBlacklistRule implements Rule {
    @Override
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        return areaConfig(ruleContext)
                .map(AreaConfig::getFileNameBlackList)
                .map(fileNameBlackList -> fileNameBlackList.contains(fileContext.getFileName()))
                .orElse(false);
    }

    private Optional<AreaConfig> areaConfig(RuleContext ruleContext) {
        if (ruleContext.getRuleConfig().getTargetAreaConfigs() == null) {
            return Optional.empty();
        }

        return ruleContext.getRuleConfig().getTargetAreaConfigs().stream()
                .filter(areaConfig -> ruleContext.getCurrentArea() == areaConfig.getArea())
                .findFirst();
    }
}

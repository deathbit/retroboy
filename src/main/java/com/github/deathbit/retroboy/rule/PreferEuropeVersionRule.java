package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;

import java.util.List;

public class PreferEuropeVersionRule implements Rule {
    private final Rule europeBaseWithoutPreference;

    public PreferEuropeVersionRule(Rule europeBaseWithoutPreference) {
        this.europeBaseWithoutPreference = europeBaseWithoutPreference;
    }

    @Override
    public RuleResult pass(RuleContext ruleContext, FileContext fileContext) {
        if (ruleContext.getCurrentArea() != Area.EUR) {
            return RuleResult.passed();
        }

        if (!europeBaseWithoutPreference.pass(ruleContext, fileContext).passed() || isEuropeVersion(fileContext)) {
            return RuleResult.passed();
        }

        var preferredEuropeVersionFileNames = preferredEuropeVersionFileNames(ruleContext, fileContext);
        if (preferredEuropeVersionFileNames.isEmpty()) {
            return RuleResult.passed();
        }

        return RuleResult.failed("存在同名 Europe 版本: " + String.join(", ", preferredEuropeVersionFileNames));
    }

    private List<String> preferredEuropeVersionFileNames(RuleContext ruleContext, FileContext fileContext) {
        if (ruleContext.getFileContextMap() == null) {
            return List.of();
        }

        return ruleContext.getFileContextMap().values().stream()
                .filter(other -> other.getNamePart().equals(fileContext.getNamePart()))
                .filter(this::isEuropeVersion)
                .filter(other -> europeBaseWithoutPreference.pass(ruleContext, other).passed())
                .map(FileContext::getFileName)
                .toList();
    }

    private boolean isEuropeVersion(FileContext fileContext) {
        return fileContext.getTags().contains("Europe");
    }
}

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
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        if (ruleContext.getCurrentArea() != Area.EUR) {
            return true;
        }

        return !europeBaseWithoutPreference.pass(ruleContext, fileContext)
                || isEuropeVersion(fileContext)
                || preferredEuropeVersionFileNames(ruleContext, fileContext).isEmpty();
    }

    private List<String> preferredEuropeVersionFileNames(RuleContext ruleContext, FileContext fileContext) {
        if (ruleContext.getFileContextMap() == null) {
            return List.of();
        }

        return ruleContext.getFileContextMap().values().stream()
                .filter(other -> other.getNamePart().equals(fileContext.getNamePart()))
                .filter(this::isEuropeVersion)
                .filter(other -> europeBaseWithoutPreference.pass(ruleContext, other))
                .map(FileContext::getFileName)
                .toList();
    }

    private boolean isEuropeVersion(FileContext fileContext) {
        return fileContext.getTags().contains("Europe");
    }
}

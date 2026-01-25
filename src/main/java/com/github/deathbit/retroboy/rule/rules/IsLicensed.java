package com.github.deathbit.retroboy.rule.rules;

import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;

public class IsLicensed implements Rule {

    @Override
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        return ruleContext.getLicensed().contains(fileContext.getFullName());
    }
}

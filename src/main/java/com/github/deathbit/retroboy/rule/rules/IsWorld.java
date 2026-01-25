package com.github.deathbit.retroboy.rule.rules;

import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;

public class IsWorld implements Rule {
    @Override
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        return fileContext.getTagPart().contains("World");
    }
}

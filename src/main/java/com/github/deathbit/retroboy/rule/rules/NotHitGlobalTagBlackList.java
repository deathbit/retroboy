package com.github.deathbit.retroboy.rule.rules;

import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;

public class NotHitGlobalTagBlackList implements Rule {

    @Override
    public boolean pass(RuleContext ruleContext, FileContext fileContext) {
        return fileContext.getTags().stream().noneMatch(tag -> ruleContext.getGlobalTagBlackList().contains(tag));
    }
}

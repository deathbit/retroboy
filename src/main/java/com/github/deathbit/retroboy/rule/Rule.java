package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;

public interface Rule {
    boolean pass(RuleContext ruleContext, FileContext fileContext);
}

package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

@FunctionalInterface
public interface Rule {
    boolean pass(RuleContext ruleContext, FileContext fileContext);

    default Rule or(Rule other) {
        return (ruleContext, fileContext) -> pass(ruleContext, fileContext) || other.pass(ruleContext, fileContext);
    }

    default Rule and(Rule other) {
        return (ruleContext, fileContext) -> pass(ruleContext, fileContext) && other.pass(ruleContext, fileContext);
    }

    default Rule not() {
        return (ruleContext, fileContext) -> !pass(ruleContext, fileContext);
    }
}

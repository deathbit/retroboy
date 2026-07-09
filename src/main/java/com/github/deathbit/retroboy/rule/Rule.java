package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

@FunctionalInterface
public interface Rule {
    boolean pass(RuleContext ruleContext, FileContext fileContext);

    default Rule and(Rule other) {
        return (rc, fc) -> this.pass(rc, fc) && other.pass(rc, fc);
    }

    default Rule or(Rule other) {
        return (rc, fc) -> this.pass(rc, fc) || other.pass(rc, fc);
    }
}

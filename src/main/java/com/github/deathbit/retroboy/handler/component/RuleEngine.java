package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.RuleContext;

public interface RuleEngine {
    void handle(RuleContext ruleContext);
}

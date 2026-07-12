package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.RuleContext;

public interface CoreHandler {
    void handle(RuleContext ruleContext) throws Exception;
}


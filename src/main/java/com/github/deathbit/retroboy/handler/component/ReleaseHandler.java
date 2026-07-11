package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.RuleContext;

public interface ReleaseHandler {
    void handle(RuleContext ruleContext) throws Exception;
}

package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.Handler;

public interface InitializeRuleStateHandlerComponent {
    void handle(Handler handler, RuleContext ruleContext);
}


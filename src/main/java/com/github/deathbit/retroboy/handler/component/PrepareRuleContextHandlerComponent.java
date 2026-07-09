package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.handler.Handler;

public interface PrepareRuleContextHandlerComponent {
    RuleContext handle(Handler handler) throws Exception;
}




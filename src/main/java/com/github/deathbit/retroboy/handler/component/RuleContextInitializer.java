package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.handler.Handler;

public interface RuleContextInitializer {
    RuleContext handle(Handler handler) throws Exception;
    void initializeArea(RuleContext ruleContext, Area area);
    void initializeFile(RuleContext ruleContext, FileContext file);
}




package com.github.deathbit.retroboy.handler.component;

import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Platform;

public interface RuleContextInitializer {
    RuleContext handle(Platform platform) throws Exception;
}




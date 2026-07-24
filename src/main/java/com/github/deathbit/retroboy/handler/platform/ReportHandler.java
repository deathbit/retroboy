package com.github.deathbit.retroboy.handler.platform;

import com.github.deathbit.retroboy.domain.RuleContext;

public interface ReportHandler {
    void handle(RuleContext ruleContext) throws Exception;
}

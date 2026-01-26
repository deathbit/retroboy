package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.RuleConfig;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

public interface Handler {
    RuleContext buildRuleContext(RuleConfig ruleConfig, AppConfig appConfig);
    FileContext buildFileContext(String fileName);
    Rule buildJapanRule();
    Rule buildUsaRule();
    Rule buildEuropeRule();
    void handle();
}

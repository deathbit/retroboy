package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;

import java.util.List;

public interface Handler {
    RuleContext buildRuleContext(RuleConfig ruleConfig, AppConfig appConfig);
    FileContext buildFileContext(String fileName);
    List<Rule> buildJapanRuleChain();
    List<Rule> buildUsaRuleChain();
    List<Rule> buildEuropeRuleChain();
    void handle();
}

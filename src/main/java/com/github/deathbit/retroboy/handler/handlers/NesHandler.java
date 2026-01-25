package com.github.deathbit.retroboy.handler.handlers;

import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.handler.Handler;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.domain.FileContext;
import com.github.deathbit.retroboy.rule.domain.RuleContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NesHandler implements Handler {

    @Override
    public RuleContext buildRuleContext(RuleConfig ruleConfig) {

        return null;
    }

    @Override
    public FileContext buildFileContext(String fileName) {
        return null;
    }

    @Override
    public List<Rule> buildJapanRuleChain() {
        return List.of();
    }

    @Override
    public List<Rule> buildUSARuleChain() {
        return List.of();
    }

    @Override
    public List<Rule> buildEuropeRuleChain() {
        return List.of();
    }

    @Override
    public void handle() {

    }
}

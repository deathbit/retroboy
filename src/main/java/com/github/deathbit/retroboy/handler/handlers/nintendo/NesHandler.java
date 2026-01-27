package com.github.deathbit.retroboy.handler.handlers.nintendo;

import java.util.Map;

import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.handler.AbstractHandler;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import org.springframework.stereotype.Component;

@Component
public class NesHandler extends AbstractHandler {

    @Override
    public Map<Area, Rule> getRuleMap() {
        return Map.of(
            Area.JPN, Rules.IS_JAPAN_BASE,
            Area.USA, Rules.IS_USA_BASE,
            Area.EUR, Rules.IS_EUROPE_BASE
        );
    }

    @Override
    public Platform getPlatform() {
        return Platform.NES;
    }
}

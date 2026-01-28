package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.domain.HandlerInput;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;

import java.util.Map;

public interface Handler {
    void handle(HandlerInput handlerInput) throws Exception;

    Map<Area, Rule> getRuleMap();

    Platform getPlatform();
}

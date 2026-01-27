package com.github.deathbit.retroboy.handler;

import java.util.Map;

import com.github.deathbit.retroboy.domain.HandlerInput;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;

public interface Handler {
    void handle(HandlerInput handlerInput) throws Exception;
    Map<Area, Rule> getRuleMap();
    Platform getPlatform();
}

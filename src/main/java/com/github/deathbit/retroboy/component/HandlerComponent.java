package com.github.deathbit.retroboy.component;

import java.util.Map;

import com.github.deathbit.retroboy.component.domain.HandlerInput;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.rule.Rule;

public interface HandlerComponent {
    void handle(HandlerInput handlerInput) throws Exception;
    Map<Area, Rule> getRuleMap();
    Platform getPlatform();
}

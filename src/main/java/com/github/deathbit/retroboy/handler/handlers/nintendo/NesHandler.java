package com.github.deathbit.retroboy.handler.handlers.nintendo;

import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.handler.AbstractHandler;
import org.springframework.stereotype.Component;

@Component
public class NesHandler extends AbstractHandler {

    @Override
    public Platform getPlatform() {
        return Platform.NES;
    }
}

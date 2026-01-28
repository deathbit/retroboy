package com.github.deathbit.retroboy.component.impl.handlers.nintendo;

import com.github.deathbit.retroboy.component.impl.handlers.AbstractHandlerComponent;
import com.github.deathbit.retroboy.enums.Platform;
import org.springframework.stereotype.Component;

@Component
public class NesHandlerComponent extends AbstractHandlerComponent {

    @Override
    public Platform getPlatform() {
        return Platform.NES;
    }
}

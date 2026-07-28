package com.github.deathbit.retroboy.platform;

import com.github.deathbit.retroboy.enums.Platform;

public interface PlatformPackHandler {
    void handle(Platform platform) throws Exception;
}

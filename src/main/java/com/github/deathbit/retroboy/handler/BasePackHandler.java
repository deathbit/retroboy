package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.enums.BasePackTask;

public interface BasePackHandler {
    String name();
    boolean enabled();
    BasePackTask task();
    void handle() throws Exception;
}

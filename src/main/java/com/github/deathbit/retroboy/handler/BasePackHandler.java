package com.github.deathbit.retroboy.handler;

public interface BasePackHandler {
    String name();
    boolean enabled();
    void handle() throws Exception;
}

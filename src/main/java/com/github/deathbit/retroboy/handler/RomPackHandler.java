package com.github.deathbit.retroboy.handler;

public interface RomPackHandler {
    void name();
    void enabled();
    void handle() throws Exception;
}

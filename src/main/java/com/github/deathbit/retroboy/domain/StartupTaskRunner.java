package com.github.deathbit.retroboy.domain;

@FunctionalInterface
public interface StartupTaskRunner {
    void run() throws Exception;
}


package com.github.deathbit.retroboy;

@FunctionalInterface
interface StartupTaskRunner {
    void run() throws Exception;
}


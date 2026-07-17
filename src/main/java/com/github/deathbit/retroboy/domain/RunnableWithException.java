package com.github.deathbit.retroboy.domain;

@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}


package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.RunnableWithException;

public interface BasePackTaskComponent {
    String name();
    boolean enabled();
    RunnableWithException runnable();
}

package com.github.deathbit.retroboy.component;

public interface ProgressBarComponent {
    void start(String taskName, int totalItems);
    void update(String itemName);
    void finish();
}

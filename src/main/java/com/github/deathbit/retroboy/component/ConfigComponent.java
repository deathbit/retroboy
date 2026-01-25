package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.config.domain.Config;

import java.util.List;

public interface ConfigComponent {
    void changeConfig(Config config);
    void batchChangeConfig(List<Config> configs);
}

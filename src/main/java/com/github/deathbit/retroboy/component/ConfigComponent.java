package com.github.deathbit.retroboy.component;

import java.util.List;

import com.github.deathbit.retroboy.domain.Config;

public interface ConfigComponent {
    void changeConfig(Config config) throws Exception;
    void batchChangeConfig(List<Config> configs) throws Exception;
}

package com.github.deathbit.retroboy.component;

import com.github.deathbit.retroboy.domain.ConfigInput;

import java.util.List;

public interface ConfigComponent {
    void batchChangeRaConfigs(List<ConfigInput> configInputs) throws Exception;
}

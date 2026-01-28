package com.github.deathbit.retroboy.component;

import java.util.List;

import com.github.deathbit.retroboy.component.domain.ConfigInput;

public interface ConfigComponent {
    void batchChangeConfigs(List<ConfigInput> configInputs) throws Exception;
}

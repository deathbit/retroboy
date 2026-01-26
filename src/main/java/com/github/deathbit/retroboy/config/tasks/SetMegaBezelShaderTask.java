package com.github.deathbit.retroboy.config.tasks;

import java.util.List;

import com.github.deathbit.retroboy.domain.Config;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetMegaBezelShaderTask {
    private CopyDirInput copyMegaBezelPacks;
    private List<CopyFileInput> copyDefaultMegaBezelShader;
    private List<Config> setMegaBezelShaderConfigs;
}

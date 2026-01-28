package com.github.deathbit.retroboy.config.tasks;

import com.github.deathbit.retroboy.domain.ConfigInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SetMegaBezelShaderTask {
    private CopyDirInput copyMegaBezelPacks;
    private List<CopyFileInput> copyDefaultMegaBezelShader;
    private List<ConfigInput> setMegaBezelShaderConfigInputs;
}

package com.github.deathbit.retroboy.config.tasks;

import java.util.List;

import com.github.deathbit.retroboy.component.domain.ConfigInput;
import com.github.deathbit.retroboy.component.domain.CopyDirContentsInput;
import com.github.deathbit.retroboy.component.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefaultConfigTask {
    private List<CopyDirContentsInput> copyContentDirs;
    private List<CopyFileInput> copyFiles;
    private List<ConfigInput> raConfigInputs;
}

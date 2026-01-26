package com.github.deathbit.retroboy.config.tasks;

import java.util.List;

import com.github.deathbit.retroboy.domain.Config;
import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefaultConfigTask {
    private List<CopyDirContentInput> copyContentDirs;
    private List<CopyFileInput> copyFiles;
    private List<Config> raConfigs;
}

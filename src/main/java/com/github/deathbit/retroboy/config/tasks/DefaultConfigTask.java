package com.github.deathbit.retroboy.config.tasks;

import com.github.deathbit.retroboy.domain.ConfigInput;
import com.github.deathbit.retroboy.domain.CopyDirContentsInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DefaultConfigTask {
    private List<CopyDirContentsInput> copyDirContentsInputs;
    private List<CopyFileInput> copyFileInputs;
    private List<ConfigInput> raConfigInputs;
}

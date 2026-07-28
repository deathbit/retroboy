package com.github.deathbit.retroboy.config.basepacktask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasePackReleaseTaskConfig {
    private String taskName;
    private boolean enabled;
    private Path targetPath;
    private List<Path> rootFilePaths;
}

package com.github.deathbit.retroboy.config.basepacktask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetUpESDEBaseTaskConfig {
    private String taskName;
    private boolean enabled;
    private String sourcePath;
    private String targetPath;
}

package com.github.deathbit.retroboy.config.basepacktask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAllTaskConfig {
    private String name;
    private boolean enabled;
    private String deleteDir;
}

package com.github.deathbit.retroboy.config.basepacktask;

import com.github.deathbit.retroboy.domain.PathPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetUpRetroArchBaseTaskConfig {
    private String taskName;
    private boolean enabled;
    private PathPair pathPair;
}

package com.github.deathbit.retroboy.config.basepacktask;

import com.github.deathbit.retroboy.domain.PathPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetUpESDEUpdateTaskConfig {
    private String taskName;
    private boolean enabled;
    private List<String> deletePaths;
    private List<PathPair> pathPairs;
}

package com.github.deathbit.retroboy.config.tasks;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CleanUpTask {

    private List<String> deleteFiles;
    private List<String> deleteContentDirs;
}

package com.github.deathbit.retroboy.config.tasks;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CleanUpTask {

    private List<String> deleteFiles;
    private List<String> deleteContentDirs;
}

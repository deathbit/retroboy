package com.github.deathbit.retroboy.component.domain;

import java.nio.file.Path;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyDirContentsInput {
    private Path srcDir;
    private Path destDir;
}

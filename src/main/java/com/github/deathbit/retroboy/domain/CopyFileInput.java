package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class CopyFileInput {
    private Path srcFile;
    private Path destDir;
}

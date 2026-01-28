package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class CopyDirContentInput {
    private Path srcDir;
    private Path destDir;
}

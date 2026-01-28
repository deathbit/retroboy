package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class RenameFileInput {
    private Path srcFile;
    private String newName;
}

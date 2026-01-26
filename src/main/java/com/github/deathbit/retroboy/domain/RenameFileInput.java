package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RenameFileInput {
    private String srcFilePath;
    private String newFileName;
}

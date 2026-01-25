package com.github.deathbit.retroboy.config.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyFile {
    private String srcFile;
    private String destDir;
}

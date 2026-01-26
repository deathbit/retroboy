package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyFileInput {
    private String srcFilePath;
    private String destDirPath;
}

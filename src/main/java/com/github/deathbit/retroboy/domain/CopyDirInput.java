package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyDirInput {
    private String srcDirPath;
    private String destDirPath;
}

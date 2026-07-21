package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleConfig {
    private Platform platform;
    private Boolean ready;
    private String romDirName;
    private String datFile;
    private String romDir;
    private String defaultCore;
    private String targetDirBase;
}

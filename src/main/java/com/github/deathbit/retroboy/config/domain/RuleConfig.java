package com.github.deathbit.retroboy.config.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleConfig {
    private String datFile;
    private String romDir;
    private String japanTargetDir;
    private String usaTargetDir;
    private String europeTargetDir;
}

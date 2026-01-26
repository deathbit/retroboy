package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RuleContext {
    private Set<String> licensed;
    private Set<String> globalTagBlackList;
    private Set<String> japanFinal;
    private Set<String> usaFinal;
    private Set<String> europeFinal;
}

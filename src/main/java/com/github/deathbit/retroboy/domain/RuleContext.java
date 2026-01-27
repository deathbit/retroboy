package com.github.deathbit.retroboy.domain;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    private Set<String> licensed;
    private Set<String> globalTagBlackList;
    private Map<String, FileContext> fileContextMap;
    private Set<String> japanFinal;
    private Set<String> usaFinal;
    private Set<String> europeFinal;
}

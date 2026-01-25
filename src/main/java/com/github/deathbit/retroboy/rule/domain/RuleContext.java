package com.github.deathbit.retroboy.rule.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RuleContext {
    private Set<String> licensed;
}

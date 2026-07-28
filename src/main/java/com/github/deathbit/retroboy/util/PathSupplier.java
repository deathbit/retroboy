package com.github.deathbit.retroboy.util;

import com.github.deathbit.retroboy.domain.RuleContext;

import java.nio.file.Path;

@FunctionalInterface
public interface PathSupplier {
    Path get(RuleContext ruleContext);
}

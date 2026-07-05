package com.github.deathbit.retroboy.rule;

import java.util.ArrayList;
import java.util.List;

public class RuleResult {
    private final boolean passed;
    private final List<String> failures;

    private RuleResult(boolean passed, List<String> failures) {
        this.passed = passed;
        this.failures = List.copyOf(failures);
    }

    public static RuleResult pass() {
        return new RuleResult(true, List.of());
    }

    public static RuleResult fail(String ruleName, String reason) {
        return new RuleResult(false, List.of(ruleName + ": " + reason));
    }

    public static RuleResult fail(RuleResult left, RuleResult right) {
        List<String> failures = new ArrayList<>();
        failures.addAll(left.getFailures());
        failures.addAll(right.getFailures());

        return new RuleResult(false, failures);
    }

    public boolean isPassed() {
        return passed;
    }

    public List<String> getFailures() {
        return failures;
    }
}

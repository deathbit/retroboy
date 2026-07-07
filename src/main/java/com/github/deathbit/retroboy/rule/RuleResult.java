package com.github.deathbit.retroboy.rule;

public record RuleResult(boolean passed, String failureReason) {
    public static RuleResult passed() {
        return new RuleResult(true, "");
    }

    public static RuleResult passed(String failureReason) {
        return new RuleResult(true, failureReason);
    }

    public static RuleResult failed(String failureReason) {
        return new RuleResult(false, failureReason);
    }
}

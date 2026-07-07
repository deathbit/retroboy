package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

@FunctionalInterface
public interface Rule {
    RuleResult pass(RuleContext ruleContext, FileContext fileContext);

    default Rule or(Rule other) {
        return (ruleContext, fileContext) -> {
            var result = pass(ruleContext, fileContext);
            if (result.passed()) {
                return RuleResult.success();
            }

            var otherResult = other.pass(ruleContext, fileContext);
            if (otherResult.passed()) {
                return RuleResult.success();
            }

            return RuleResult.failed(combineFailureReasons(result.failureReason(), otherResult.failureReason()));
        };
    }

    default Rule and(Rule other) {
        return (ruleContext, fileContext) -> {
            var result = pass(ruleContext, fileContext);
            if (!result.passed()) {
                return result;
            }

            return other.pass(ruleContext, fileContext);
        };
    }

    default Rule not() {
        return (ruleContext, fileContext) -> {
            var result = pass(ruleContext, fileContext);
            if (result.passed()) {
                return RuleResult.failed(result.failureReason().isBlank() ? "命中排除规则" : result.failureReason());
            }

            return RuleResult.success();
        };
    }

    private static String combineFailureReasons(String first, String second) {
        if (first == null || first.isBlank()) {
            return second == null ? "" : second;
        }
        if (second == null || second.isBlank()) {
            return first;
        }

        return first + "；" + second;
    }
}

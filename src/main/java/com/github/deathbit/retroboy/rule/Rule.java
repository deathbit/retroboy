package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.RuleContext;

import java.util.function.BiFunction;

public interface Rule {
    boolean pass(RuleContext ruleContext, FileContext fileContext);

    default RuleResult evaluate(RuleContext ruleContext, FileContext fileContext) {
        if (pass(ruleContext, fileContext)) {
            return RuleResult.pass();
        }

        return RuleResult.fail(describe(), "规则未通过");
    }

    default String describe() {
        return getClass().getSimpleName();
    }

    default Rule or(Rule other) {
        return composite("OR", this, other);
    }

    default Rule and(Rule other) {
        return composite("AND", this, other);
    }

    static Rule named(String name, Rule rule, String failureReason) {
        return named(name, rule, (ruleContext, fileContext) -> failureReason);
    }

    static Rule named(String name, Rule rule, BiFunction<RuleContext, FileContext, String> failureReason) {
        return new Rule() {
            @Override
            public boolean pass(RuleContext ruleContext, FileContext fileContext) {
                return rule.pass(ruleContext, fileContext);
            }

            @Override
            public RuleResult evaluate(RuleContext ruleContext, FileContext fileContext) {
                if (pass(ruleContext, fileContext)) {
                    return RuleResult.pass();
                }

                return RuleResult.fail(name, failureReason.apply(ruleContext, fileContext));
            }

            @Override
            public String describe() {
                return name;
            }
        };
    }

    private static Rule composite(String operator, Rule left, Rule right) {
        return new Rule() {
            @Override
            public boolean pass(RuleContext ruleContext, FileContext fileContext) {
                if ("AND".equals(operator)) {
                    return left.pass(ruleContext, fileContext) && right.pass(ruleContext, fileContext);
                }

                return left.pass(ruleContext, fileContext) || right.pass(ruleContext, fileContext);
            }

            @Override
            public RuleResult evaluate(RuleContext ruleContext, FileContext fileContext) {
                RuleResult leftResult = left.evaluate(ruleContext, fileContext);
                RuleResult rightResult = right.evaluate(ruleContext, fileContext);

                if ("AND".equals(operator)) {
                    if (leftResult.isPassed() && rightResult.isPassed()) {
                        return RuleResult.pass();
                    }

                    return RuleResult.fail(leftResult, rightResult);
                }

                if (leftResult.isPassed() || rightResult.isPassed()) {
                    return RuleResult.pass();
                }

                return RuleResult.fail(leftResult, rightResult);
            }

            @Override
            public String describe() {
                return left.describe() + " " + operator + " " + right.describe();
            }
        };
    }
}

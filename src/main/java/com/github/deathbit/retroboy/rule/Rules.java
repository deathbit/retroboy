package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.rule.rules.*;

public class Rules {
    public static final Rule COMMON_RULE = new IsLicensed().and(new IsNotBad()).and(new IsNotBIOS());
    public static final Rule IS_JAPAN_LICENSED = COMMON_RULE.and(new IsJapan().or(new IsWorld()));
    public static final Rule IS_USA_LICENSED = COMMON_RULE.and(new IsUsa().or(new IsWorld()));
    public static final Rule IS_EUROPE_LICENSED = COMMON_RULE.and(new IsEurope().or(new IsWorld()));
}

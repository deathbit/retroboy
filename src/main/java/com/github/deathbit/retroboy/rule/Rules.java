package com.github.deathbit.retroboy.rule;

import com.github.deathbit.retroboy.rule.rules.*;

public class Rules {
    public static final Rule IS_JAPAN_LICENSED = new IsLicensed().and(new IsJapan().or(new IsWorld()));
    public static final Rule IS_USA_LICENSED = new IsLicensed().and(new IsUSA().or(new IsWorld()));
    public static final Rule IS_EUROPE_LICENSED = new IsLicensed().and(new IsEurope().or(new IsWorld()));
}

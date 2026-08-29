package com.github.deathbit.retroboy.enums;

import lombok.Getter;

@Getter
public enum Area {
    JPN("日本"),
    EUR("欧洲"),
    USA("北美"),
    AUS("澳大利亚"),
    GER("德国"),
    FRA("法国"),
    SWE("瑞典"),
    UK("英国");

    private final String chineseName;

    Area(String chineseName) {
        this.chineseName = chineseName;
    }
}

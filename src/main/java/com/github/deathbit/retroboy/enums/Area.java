package com.github.deathbit.retroboy.enums;

import lombok.Getter;

@Getter
public enum Area {
    JPN("日本"),
    USA("北美"),
    EUR("欧洲");

    private final String chineseName;

    Area(String chineseName) {
        this.chineseName = chineseName;
    }
}

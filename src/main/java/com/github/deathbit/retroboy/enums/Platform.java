package com.github.deathbit.retroboy.enums;

import lombok.Getter;

@Getter
public enum Platform {
    NES("Nintendo Entertainment System", "Nintendo"),
    FDS("Famicom Disk System", "Nintendo");

    private final String systemFullName;
    private final String company;

    Platform(String systemFullName, String company) {
        this.systemFullName = systemFullName;
        this.company = company;
    }
}

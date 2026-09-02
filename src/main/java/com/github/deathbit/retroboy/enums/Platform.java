package com.github.deathbit.retroboy.enums;

import lombok.Getter;

@Getter
public enum Platform {
    NES("Nintendo Entertainment System", "Nintendo", "nes"),
    FDS("Famicom Disk System", "Nintendo", "fds");

    private final String systemFullName;
    private final String company;
    private final String name;

    Platform(String systemFullName, String company, String name) {
        this.systemFullName = systemFullName;
        this.company = company;
        this.name = name;
    }
}

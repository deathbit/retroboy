package com.github.deathbit.retroboy.domain;

public final class MediaType {
    private final String name;
    private final String reportExtension;
    private final String fallbackExtension;

    public MediaType(String name, String reportExtension, String fallbackExtension) {
        this.name = name;
        this.reportExtension = reportExtension;
        this.fallbackExtension = fallbackExtension;
    }

    public String name() {
        return name;
    }

    public String reportExtension() {
        return reportExtension;
    }

    public String fallbackExtension() {
        return fallbackExtension;
    }
}


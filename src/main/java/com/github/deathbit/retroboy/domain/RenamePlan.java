package com.github.deathbit.retroboy.domain;

public final class RenamePlan {
    private final FileContext fileContext;
    private final String targetFileName;
    private final boolean articleAdjusted;

    public RenamePlan(FileContext fileContext, String targetFileName, boolean articleAdjusted) {
        this.fileContext = fileContext;
        this.targetFileName = targetFileName;
        this.articleAdjusted = articleAdjusted;
    }

    public FileContext fileContext() {
        return fileContext;
    }

    public String targetFileName() {
        return targetFileName;
    }

    public boolean articleAdjusted() {
        return articleAdjusted;
    }
}


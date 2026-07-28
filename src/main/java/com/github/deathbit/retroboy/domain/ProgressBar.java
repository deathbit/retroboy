package com.github.deathbit.retroboy.domain;

import lombok.Data;

@Data
public class ProgressBar {
    private static final long DEFAULT_REFRESH_INTERVAL_MILLIS = 100L;

    private String mainTaskName;
    private Integer taskIndex;
    private Integer taskTotal;
    private Double currentPercentage;
    private Integer barWidth;
    private Long refreshIntervalMillis;
    private Long lastRenderTimeMillis;

    public ProgressBar(String mainTaskName) {
        this(mainTaskName, DEFAULT_REFRESH_INTERVAL_MILLIS);
    }

    public ProgressBar(String mainTaskName, long refreshIntervalMillis) {
        this.mainTaskName = mainTaskName;
        this.taskIndex = 0;
        this.taskTotal = 0;
        this.currentPercentage = 0.0;
        this.barWidth = 20;
        this.refreshIntervalMillis = Math.max(0L, refreshIntervalMillis);
        this.lastRenderTimeMillis = 0L;
    }

    private static String bar(double percentage, int width) {
        percentage = clamp(percentage);
        int filled = (int) Math.floor(width * percentage);
        int empty = width - filled;

        return "█".repeat(Math.max(0, filled)) + "░".repeat(Math.max(0, empty)) +
                String.format(" %6.2f%%", percentage * 100);
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    public void startTask(Integer currentTaskTotal) {
        this.taskTotal = Math.max(0, currentTaskTotal);
        this.taskIndex = 0;
        currentPercentage = 0.0;
        renderProgressBar(true);
    }

    public void updateTask(Integer currentTaskIndex) {
        if (taskTotal == 0) {
            this.taskIndex = 0;
            this.currentPercentage = 1.0;
            renderProgressBar(true);
            return;
        }
        this.taskIndex = Math.max(0, Math.min(currentTaskIndex, taskTotal - 1));
        this.currentPercentage = clamp((double) (this.taskIndex + 1) / this.taskTotal);
        renderProgressBar(false);
    }

    public void finishTask() {
        if (taskTotal > 0) {
            taskIndex = taskTotal - 1;
        }
        currentPercentage = 1.0;
        renderProgressBar(true);
    }

    public void close() {
        System.out.println();
        System.out.flush();
    }

    public void finishTaskAndClose() {
        finishTask();
        close();
    }

    private void renderProgressBar(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastRenderTimeMillis < refreshIntervalMillis) {
            return;
        }
        lastRenderTimeMillis = now;

        int finishedCount = taskTotal == 0 ? 0 : taskIndex + 1;
        String line = String.format("\r| %s %s %12s",
                mainTaskName,
                bar(currentPercentage, barWidth),
                String.format("%d/%d", finishedCount, taskTotal)
        );
        System.out.print(line);
        System.out.flush();
    }
}
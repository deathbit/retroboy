package com.github.deathbit.retroboy.domain;

import lombok.Data;

@Data
public class ProgressBar {
    private String mainTaskName;
    private Integer subTaskCount;
    private Integer currentSubTaskIndex;
    private Integer currentSubTaskTotal;
    private Double currentPercentage;
    private Integer finishedTasks;
    private Integer barWidth;

    public ProgressBar(String mainTaskName) {
        this(mainTaskName, 1);
    }

    public ProgressBar(String mainTaskName, Integer subTaskCount) {
        this.mainTaskName = mainTaskName;
        this.subTaskCount = subTaskCount;
        this.currentSubTaskIndex = 0;
        this.currentSubTaskTotal = 0;
        this.currentPercentage = 0.0;
        this.finishedTasks = 0;
        this.barWidth = 20;
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

    public void startTask(Integer currentSubTaskTotal) {
        this.currentSubTaskTotal = currentSubTaskTotal;
        currentPercentage = 0.0;
        renderProgressBar();
    }

    public void updateTask(Integer currentSubTaskIndex) {
        this.currentSubTaskIndex = currentSubTaskIndex;
        this.currentPercentage = clamp((double) (currentSubTaskIndex + 1) / this.currentSubTaskTotal);
        renderProgressBar();
    }

    public void finishTask() {
        currentPercentage = 1.0;
        renderProgressBar();
        finishedTasks++;
        renderProgressBar();
    }

    public void close() {
        System.out.println();
        System.out.flush();
    }

    public void finishTaskAndClose() {
        finishTask();
        close();
    }

    private void renderProgressBar() {
        double totalPercentage = (double) finishedTasks / subTaskCount;
        String line = String.format("\r| %s %s %5s | %s %10s",
                mainTaskName,
                bar(totalPercentage, barWidth),
                String.format("%d/%d", finishedTasks, subTaskCount),
                bar(currentPercentage, barWidth),
                String.format("%d/%d", currentSubTaskIndex + 1, currentSubTaskTotal)
        );
        System.out.print(line);
        System.out.flush();
    }
}
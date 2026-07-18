package com.github.deathbit.retroboy.domain;

import lombok.Data;

@Data
public class ProgressBar {
    private String mainTaskName;
    private Integer taskIndex;
    private Integer taskTotal;
    private Double currentPercentage;
    private Integer barWidth;

    public ProgressBar(String mainTaskName) {
        this.mainTaskName = mainTaskName;
        this.taskIndex = 0;
        this.taskTotal = 0;
        this.currentPercentage = 0.0;
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

    public void startTask(Integer currentTaskTotal) {
        this.taskTotal = Math.max(0, currentTaskTotal);
        this.taskIndex = 0;
        currentPercentage = 0.0;
        renderProgressBar();
    }

    public void updateTask(Integer currentTaskIndex) {
        if (taskTotal == 0) {
            this.taskIndex = 0;
            this.currentPercentage = 1.0;
            renderProgressBar();
            return;
        }
        this.taskIndex = Math.max(0, Math.min(currentTaskIndex, taskTotal - 1));
        this.currentPercentage = clamp((double) (this.taskIndex + 1) / this.taskTotal);
        renderProgressBar();
    }

    public void finishTask() {
        if (taskTotal > 0) {
            taskIndex = taskTotal - 1;
        }
        currentPercentage = 1.0;
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
        int finishedCount = taskTotal == 0 ? 0 : taskIndex + 1;
        String line = String.format("\r| %s %s %12s",
                mainTaskName,
                bar(currentPercentage, barWidth),
                String.format("%d/%d", finishedCount, taskTotal)
        );
        System.out.print(line);
        System.out.flush();
    }

    public void done() {
        this.startTask(1);
        this.updateTask(0);
        this.finishTaskAndClose();
    }
}
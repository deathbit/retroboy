package com.github.deathbit.retroboy.utils;

import java.util.List;

public class SingleLineHierarchicalProgressBar {
    private final List<String> tasks;
    private int doneTasks = 0;
    private String currentTask = "";
    private double currentPct = 0.0;

    public SingleLineHierarchicalProgressBar(List<String> tasks) {
        this.tasks = tasks;
    }

    public synchronized void startTask(String name) {
        currentTask = name;
        currentPct = 0.0;
        render();
    }

    public synchronized void updateTask(double pct) {
        currentPct = clamp01(pct);
        render();
    }

    public synchronized void finishTask() {
        currentPct = 1.0;
        render();
        doneTasks++;
        render();
    }

    public synchronized void close() {
        System.out.print("\n");
        System.out.flush();
    }

    private void render() {
        double totalPct = tasks.isEmpty() ? 1.0 : (double) doneTasks / tasks.size();

        String line =
            "TOTAL " + doneTasks + "/" + tasks.size() + " " + bar(totalPct, 20) +
                " | SUB " + padRight(currentTask, 12) + " " + bar(currentPct, 20);

        // \r 回到行首覆盖；后面补空格避免残留
        System.out.print("\r" + line + "        ");
        System.out.flush();
    }

    private static String bar(double pct, int width) {
        pct = clamp01(pct);
        int filled = (int) Math.floor(width * pct);
        int empty = width - filled;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("█".repeat(Math.max(0, filled)));
        sb.append("░".repeat(Math.max(0, empty)));
        sb.append(']');
        sb.append(String.format(" %6.2f%%", pct * 100));
        return sb.toString();
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        return s + " ".repeat(n - s.length());
    }

    // Demo
    public static void main(String[] args) throws Exception {
        List<String> tasks = List.of("download", "extract", "process");
        SingleLineHierarchicalProgressBar p = new SingleLineHierarchicalProgressBar(tasks);

        for (String t : tasks) {
            p.startTask(t);
            for (int i = 0; i <= 100; i++) {
                p.updateTask(i / 100.0);
                Thread.sleep(20);
            }
            p.finishTask();
        }

        p.close();
    }
}
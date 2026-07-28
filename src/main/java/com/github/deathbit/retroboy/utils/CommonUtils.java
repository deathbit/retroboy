package com.github.deathbit.retroboy.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    private static final String HORIZONTAL_LINE = "+" + "-".repeat(200) + "+";
    private static final Map<String, Long> TASK_START_TIMES = new ConcurrentHashMap<>();

    public static void printTask(String mainTaskName) {
        TASK_START_TIMES.put(mainTaskName, System.nanoTime());
        System.out.println();
        System.out.println(HORIZONTAL_LINE);
        System.out.println("| " + mainTaskName);
        System.out.println(HORIZONTAL_LINE);
    }

    public static void printTaskDone(String taskName) {
        System.out.println(HORIZONTAL_LINE);
        System.out.println("| " + taskName + " 完成 - 耗时: " + elapsedTime(taskName));
        System.out.println(HORIZONTAL_LINE);
    }

    private static String elapsedTime(String taskName) {
        Long startTime = TASK_START_TIMES.remove(taskName);
        if (startTime == null) {
            return "0分0秒";
        }

        long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
        return "%d分%d秒".formatted(elapsedSeconds / 60, elapsedSeconds % 60);
    }

    public static void printAsciiArt() {
        System.out.println(HORIZONTAL_LINE);
        System.out.print("""
                ▗▄▄▖ ▗▄▄▄▖▗▄▄▄▖▗▄▄▖  ▗▄▖ ▗▄▄▖  ▗▄▖▗▖  ▗▖
                ▐▌ ▐▌▐▌     █  ▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▝▚▞▘\s
                ▐▛▀▚▖▐▛▀▀▘  █  ▐▛▀▚▖▐▌ ▐▌▐▛▀▚▖▐▌ ▐▌ ▐▌ \s
                ▐▌ ▐▌▐▙▄▄▖  █  ▐▌ ▐▌▝▚▄▞▘▐▙▄▞▘▝▚▄▞▘ ▐▌  by 村长不可爱\s
                """);
        System.out.println(HORIZONTAL_LINE);
    }
}

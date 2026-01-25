package com.github.deathbit.retroboy;

import java.util.List;

public class Utils {

    public static void printTaskArt(String mainTaskName, List<String> subTaskNames) {
        String title = "主任务: " + mainTaskName;
        String subTitle = "子任务";

        String border = "+" + "-".repeat(100) + "+";
        System.out.println();
        System.out.println(border);
        System.out.println("| " + title);
        System.out.println(border);
        System.out.println("| " + subTitle);

        if (subTaskNames != null && !subTaskNames.isEmpty()) {
            for (int i = 0; i < subTaskNames.size(); i++) {
                String index = i >= 10 ? String.valueOf(i) : " " + i;
                String line = index + ". " + subTaskNames.get(i);
                System.out.println("| " + line);
            }
        }

        System.out.println(border);
    }
}

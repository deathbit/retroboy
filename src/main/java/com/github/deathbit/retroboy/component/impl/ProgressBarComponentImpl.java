package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ProgressBarComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProgressBarComponentImpl implements ProgressBarComponent {
    
    private static final int PROGRESS_BAR_WIDTH = 50;
    private static final String PROGRESS_CHAR = "█";
    private static final String REMAINING_CHAR = "░";
    
    private String taskName;
    private int totalItems;
    private int currentItem;
    
    @Override
    public void start(String taskName, int totalItems) {
        if (totalItems < 0) {
            log.warn("Total items cannot be negative: {}, using 0 instead", totalItems);
            totalItems = 0;
        }
        
        this.taskName = taskName;
        this.totalItems = totalItems;
        this.currentItem = 0;
        
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println(taskName);
        System.out.println("=".repeat(60));
        printProgressBar();
    }
    
    @Override
    public void update(String itemName) {
        currentItem++;
        printProgressBar();
        // Print a newline and the item name after the progress bar
        if (itemName != null && !itemName.isEmpty()) {
            System.out.println();
            System.out.println("  Processing: " + itemName);
        }
    }
    
    @Override
    public void finish() {
        System.out.println();
        System.out.println("✓ " + taskName + " completed!");
        System.out.println("=".repeat(60));
        System.out.println();
    }
    
    private void printProgressBar() {
        if (totalItems <= 0) {
            return;
        }
        
        int progress = (int) ((double) currentItem / totalItems * PROGRESS_BAR_WIDTH);
        int remaining = PROGRESS_BAR_WIDTH - progress;
        
        StringBuilder bar = new StringBuilder();
        bar.append("\r[");
        bar.append(PROGRESS_CHAR.repeat(Math.max(0, progress)));
        bar.append(REMAINING_CHAR.repeat(Math.max(0, remaining)));
        bar.append("] ");
        
        int percentage = (int) ((double) currentItem / totalItems * 100);
        bar.append(String.format("%3d%% (%d/%d)", percentage, currentItem, totalItems));
        
        System.out.print(bar.toString());
        System.out.flush();
    }
}

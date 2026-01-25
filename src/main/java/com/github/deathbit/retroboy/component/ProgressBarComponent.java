package com.github.deathbit.retroboy.component;

public interface ProgressBarComponent {
    /**
     * Initialize a progress bar with a given task name and total items
     * @param taskName Name of the task being performed
     * @param totalItems Total number of items to process
     */
    void start(String taskName, int totalItems);
    
    /**
     * Update the progress bar by one step
     * @param itemName Name of the item being processed (optional)
     */
    void update(String itemName);
    
    /**
     * Complete the progress bar
     */
    void finish();
}

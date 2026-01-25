package com.github.deathbit.retroboy.component;

import java.util.List;

public interface CleanUpComponent {
    void deleteDir(String dir);
    void deleteFile(String file);
    void cleanupDir(String dir);
    void batchDeleteDir(List<String> dirs);
    void batchDeleteFile(List<String> files);
    void batchCleanupDir(List<String> dirs);
}

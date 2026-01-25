package com.github.deathbit.retroboy.component;

import java.util.List;

public interface CleanUpComponent {
    void deleteDir(String dir);
    void deleteFile(String file);
    void batchDeleteDir(List<String> dirs);
    void batchDdeleteFile(List<String> files);
}

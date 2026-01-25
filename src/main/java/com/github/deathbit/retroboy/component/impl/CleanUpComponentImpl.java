package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 清理组件实现
 * 用于删除指定的多个目录下的所有内容
 */
@Slf4j
@Component
public class CleanUpComponentImpl implements CleanUpComponent {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void cleanup() {
        var cleanupDirs = appConfig.getCleanup().getCleanupDirs();
        if (cleanupDirs == null || cleanupDirs.isEmpty()) {
            log.warn("清理目录未配置，跳过清理操作");
            return;
        }

        for (String cleanupDirectory : cleanupDirs) {
            if (cleanupDirectory == null || cleanupDirectory.trim().isEmpty()) {
                log.warn("跳过空的清理目录配置");
                continue;
            }

            Path dirPath = Paths.get(cleanupDirectory);

            if (!Files.exists(dirPath)) {
                log.info("清理目录不存在: {}", cleanupDirectory);
                continue;
            }

            if (!Files.isDirectory(dirPath)) {
                log.warn("指定的路径不是目录: {}", cleanupDirectory);
                continue;
            }

            try {
                log.info("开始清理目录: {}", cleanupDirectory);
                deleteDirectoryContents(dirPath);
                log.info("目录清理完成: {}", cleanupDirectory);
            } catch (IOException e) {
                log.error("清理目录时发生错误: {}", cleanupDirectory, e);
            }
        }
    }

    /**
     * 递归删除目录中的所有内容
     */
    private void deleteDirectoryContents(Path directory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    deleteDirectoryContents(entry);
                    Files.delete(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
    }
}

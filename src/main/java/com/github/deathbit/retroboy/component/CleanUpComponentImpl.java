package com.github.deathbit.retroboy.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 清理组件实现
 * 用于删除指定目录下的所有内容
 */
@Slf4j
@Component
public class CleanUpComponentImpl implements CleanUpComponent {

    @Value("${cleanup.directory}")
    private String cleanupDirectory;

    @Override
    public void clean() {
        if (cleanupDirectory == null || cleanupDirectory.trim().isEmpty()) {
            log.warn("清理目录未配置，跳过清理操作");
            return;
        }

        Path dirPath = Paths.get(cleanupDirectory);
        
        if (!Files.exists(dirPath)) {
            log.info("清理目录不存在: {}", cleanupDirectory);
            return;
        }

        if (!Files.isDirectory(dirPath)) {
            log.warn("指定的路径不是目录: {}", cleanupDirectory);
            return;
        }

        try {
            log.info("开始清理目录: {}", cleanupDirectory);
            deleteDirectoryContents(dirPath);
            log.info("目录清理完成: {}", cleanupDirectory);
        } catch (IOException e) {
            log.error("清理目录时发生错误: {}", cleanupDirectory, e);
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

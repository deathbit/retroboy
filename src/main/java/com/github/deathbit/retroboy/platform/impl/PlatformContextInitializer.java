package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class PlatformContextInitializer {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Map<Platform, PlatformProcessor> platformProcessorMap;

    public PlatformContext handle(Platform platform) throws Exception {
        var platformContext = new PlatformContext();
        // set platform
        platformContext.setPlatform(platform);
        platformContext.setPlatformName(platform.name().toLowerCase());
        platformContext.setPlatformProcessor(platformProcessorMap.get(platform));
        // set config
        platformContext.setAppConfig(appConfig);
        platformContext.setGlobalConfig(appConfig.getGlobalConfig());
        platformContext.setPlatformPackTaskConfig(appConfig.getPlatformPackTaskConfigMap().get(platform));
        // set file context map
        platformContext.setFileContextMap(parseFileContextMap(
            PathUtils.string(PathUtils.PLATFORM_ROMS, platformContext),
            platformContext.getPlatformPackTaskConfig().getFileContextMappingList()
        ));

        return platformContext;
    }


    private Map<String, FileContext> parseFileContextMap(String romDirPath, List<String> mappingList) {
        ProgressBar pb = new ProgressBar("解析文件");
        var files = new File(romDirPath).listFiles();
        if (files == null) {
            return new HashMap<>();
        }

        var fileContextMap = new HashMap<String, FileContext>();
        Arrays.sort(files, Comparator.comparing(File::getName));
        pb.startTask(files.length);
        for (int i = 0; i < files.length; i++) {
            var file = files[i];
            var fileContext = buildFileContext(file.getName());
            fileContextMap.put(fileContext.getFullName(), fileContext);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
        applyFileContextMapping(fileContextMap, mappingList);

        return fileContextMap;
    }

    private void applyFileContextMapping(Map<String, FileContext> fileContextMap, List<String> mappingList) {
        if (mappingList == null || mappingList.isEmpty()) {
            return;
        }

        var seenAliasNames = new HashSet<String>();
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            if (parts.length != 2) {
                throw new RuntimeException("fileContextMappingList 格式错误: " + mapping);
            }
            var aliasName = parts[1].trim();
            if (!seenAliasNames.add(aliasName)) {
                throw new RuntimeException("fileContextMappingList 中别名重复: " + aliasName);
            }
        }

        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            var sourceName = parts[0].trim();
            var aliasName = parts[1].trim();

            var fileContext = fileContextMap.get(sourceName);
            if (fileContext == null) {
                throw new RuntimeException("fileContextMappingList 源文件不存在: " + sourceName);
            }
            var existingFileContext = fileContextMap.get(aliasName);
            if (existingFileContext != null && existingFileContext != fileContext) {
                throw new RuntimeException("fileContextMappingList 别名已存在: " + aliasName);
            }
            fileContextMap.put(aliasName, fileContext);
        }
    }

    private FileContext buildFileContext(String fileName) {
        var fullName = fileName;
        var ext = "";
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fullName = fileName.substring(0, dotIndex);
            ext = fileName.substring(dotIndex);
        }

        var namePart = fullName;
        var tagPart = "";
        var tags = new HashSet<String>();

        var firstParen = fullName.indexOf('(');
        if (firstParen != -1) {
            namePart = fullName.substring(0, firstParen).trim();
            tagPart = fullName.substring(firstParen);
            int start = 0;
            while ((start = tagPart.indexOf('(', start)) != -1) {
                int end = tagPart.indexOf(')', start);
                if (end == -1) {
                    break;
                }
                tags.add(tagPart.substring(start + 1, end));
                start = end + 1;
            }
        }

        return FileContext.builder()
                          .fileName(fileName)
                          .fullName(fullName)
                          .namePart(namePart)
                          .tagPart(tagPart)
                          .tags(tags)
                          .extension(ext)
                          .build();
    }
}

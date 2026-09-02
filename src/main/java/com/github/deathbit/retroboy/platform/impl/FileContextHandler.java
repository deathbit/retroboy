package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.FileContext;
import com.github.deathbit.retroboy.domain.PlatformContext;
import com.github.deathbit.retroboy.domain.ProgressBar;
import com.github.deathbit.retroboy.util.FileContextUtils;
import com.github.deathbit.retroboy.util.HashUtils;
import com.github.deathbit.retroboy.util.PathUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Component
public class FileContextHandler {

    public void handle(PlatformContext platformContext) throws Exception {
        platformContext.setFileContexts(parseFileContexts(
            PathUtils.string(PathUtils.PLATFORM_ROMS, platformContext),
            platformContext.getPlatformPackTaskConfig().getFileContextMappingList()
        ));
    }

    private List<FileContext> parseFileContexts(String romDirPath, List<String> mappingList) throws Exception {
        ProgressBar pb = new ProgressBar("解析文件");
        var files = new File(romDirPath).listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        var fileContexts = new ArrayList<FileContext>();
        Arrays.sort(files, Comparator.comparing(File::getName));
        pb.startTask(files.length);
        for (int i = 0; i < files.length; i++) {
            var file = files[i];
            var fileContext = buildFileContext(file);
            fileContexts.add(fileContext);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
        applyFileContextMapping(fileContexts, mappingList);

        return fileContexts;
    }

    private void applyFileContextMapping(List<FileContext> fileContexts, List<String> mappingList) {
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

        var fileContextLookupMap = FileContextUtils.buildLookupMap(fileContexts);
        for (var mapping : mappingList) {
            var parts = mapping.split("\\s*->\\s*", 2);
            var sourceName = parts[0].trim();
            var aliasName = parts[1].trim();

            var fileContext = fileContextLookupMap.get(sourceName);
            if (fileContext == null) {
                throw new RuntimeException("fileContextMappingList 源文件不存在: " + sourceName);
            }
            var existingFileContext = fileContextLookupMap.get(aliasName);
            if (existingFileContext != null && existingFileContext != fileContext) {
                throw new RuntimeException("fileContextMappingList 别名已存在: " + aliasName);
            }
            fileContext.getAliasNames().add(aliasName);
            fileContextLookupMap.put(aliasName, fileContext);
        }
    }

    private FileContext buildFileContext(File file) throws Exception {
        var fileName = file.getName();
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

        var fileContext = FileContext.builder()
                                     .fileName(fileName)
                                     .fullName(fullName)
                                     .aliasNames(new HashSet<>())
                                     .namePart(namePart)
                                     .tagPart(tagPart)
                                     .tags(tags)
                                     .extension(ext)
                                     .build();
        fileContext.setSha1(HashUtils.calculateSha1(file));
        return fileContext;
    }
}


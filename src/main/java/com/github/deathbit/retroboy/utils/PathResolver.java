package com.github.deathbit.retroboy.utils;

import com.github.deathbit.retroboy.config.GlobalConfig;
import com.github.deathbit.retroboy.domain.Config;
import com.github.deathbit.retroboy.domain.CopyDirContentInput;
import com.github.deathbit.retroboy.domain.CopyDirInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class PathResolver {

    private final GlobalConfig globalConfig;

    public PathResolver(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public String resolveRaPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return globalConfig.getRaHome();
        }
        return Paths.get(globalConfig.getRaHome(), relativePath).toString();
    }

    public String resolveResourcesPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return globalConfig.getResourcesHome();
        }
        return Paths.get(globalConfig.getResourcesHome(), relativePath).toString();
    }

    public String resolveEsdePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return globalConfig.getEsdeHome();
        }
        return Paths.get(globalConfig.getEsdeHome(), relativePath).toString();
    }

    // Helper methods for different types of inputs
    public List<String> resolveRaPaths(List<String> relativePaths) {
        return relativePaths.stream()
                .map(this::resolveRaPath)
                .collect(Collectors.toList());
    }

    public List<CopyDirContentInput> resolveCopyDirContentInputs(List<CopyDirContentInput> inputs) {
        return inputs.stream()
                .map(input -> CopyDirContentInput.builder()
                        .srcDir(resolveResourcesPath(input.getSrcDir()))
                        .destDir(resolveRaPath(input.getDestDir()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<CopyFileInput> resolveCopyFileInputs(List<CopyFileInput> inputs) {
        return inputs.stream()
                .map(input -> CopyFileInput.builder()
                        .srcFile(resolveResourcesPath(input.getSrcFile()))
                        .destDir(resolveRaPath(input.getDestDir()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<Config> resolveRaConfigs(List<Config> configs) {
        return configs.stream()
                .map(config -> {
                    Config.ConfigBuilder builder = Config.builder()
                            .file(resolveRaPath(config.getFile()))
                            .key(config.getKey());
                    
                    // Special handling for rgui_browser_directory - it needs esdeHome
                    if ("rgui_browser_directory".equals(config.getKey())) {
                        builder.value(resolveEsdePath(config.getValue()));
                    } else {
                        builder.value(config.getValue());
                    }
                    
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    public CopyFileInput resolveCopyFileInput(CopyFileInput input) {
        return CopyFileInput.builder()
                .srcFile(resolveResourcesPath(input.getSrcFile()))
                .destDir(resolveRaPath(input.getDestDir()))
                .build();
    }

    public Config resolveRaConfig(Config config) {
        return Config.builder()
                .file(resolveRaPath(config.getFile()))
                .key(config.getKey())
                .value(config.getValue())
                .build();
    }

    public CopyDirInput resolveCopyDirInput(CopyDirInput input) {
        return CopyDirInput.builder()
                .srcDir(resolveResourcesPath(input.getSrcDir()))
                .destDir(resolveRaPath(input.getDestDir()))
                .build();
    }
}

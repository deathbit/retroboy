package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CreateComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class CreateComponentImpl implements CreateComponent {
    
    private final ProgressBarComponent progressBarComponent;
    
    public CreateComponentImpl(ProgressBarComponent progressBarComponent) {
        this.progressBarComponent = progressBarComponent;
    }

    @Override
    public void createDir(String dir) {
        if (dir == null || dir.trim().isEmpty()) {
            System.out.println("Directory path is null or empty, skipping creation");
            return;
        }
        
        Path dirPath = Paths.get(dir);
        
        try {
            if (Files.exists(dirPath)) {
                return;
            }
            
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + dir + " - " + e.getMessage());
        }
    }

    @Override
    public void batchCreateDir(List<String> dirs) {
        if (dirs == null || dirs.isEmpty()) {
            System.out.println("No directories to create");
            return;
        }
        
        progressBarComponent.start("Batch Create Directories", dirs.size());
        
        for (String dir : dirs) {
            createDir(dir);
            progressBarComponent.update("创建目录：" + dir);
        }
        
        progressBarComponent.finish();
    }
}

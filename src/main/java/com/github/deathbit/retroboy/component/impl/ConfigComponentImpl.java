package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import com.github.deathbit.retroboy.config.domain.Config;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ConfigComponentImpl implements ConfigComponent {
    
    private final ProgressBarComponent progressBarComponent;
    
    public ConfigComponentImpl(ProgressBarComponent progressBarComponent) {
        this.progressBarComponent = progressBarComponent;
    }

    @Override
    public void changeConfig(Config config) {
        if (config == null) {
            System.out.println("Config is null, skipping config change");
            return;
        }
        
        String configFile = config.getConfigFile();
        String key = config.getKey();
        String value = config.getValue();
        
        if (configFile == null || configFile.trim().isEmpty()) {
            System.out.println("Config file path is null or empty, skipping config change");
            return;
        }
        
        if (key == null || key.trim().isEmpty()) {
            System.out.println("Config key is null or empty, skipping config change");
            return;
        }
        
        if (value == null) {
            System.out.println("Config value is null, skipping config change");
            return;
        }
        
        Path configPath = Paths.get(configFile);
        
        if (!Files.exists(configPath)) {
            System.out.println("Config file does not exist: " + configFile);
            return;
        }
        
        if (Files.isDirectory(configPath)) {
            System.out.println("Config path is a directory, not a file: " + configFile);
            return;
        }
        
        try {
            // Read all lines from the config file
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            
            boolean keyFound = false;
            
            // Process each line to find and update the key
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String trimmedLine = line.trim();
                
                // Skip empty lines and comments
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }
                
                // Check if this line contains the key we're looking for
                // Format: key = "value" or key = value
                int equalsIndex = line.indexOf('=');
                if (equalsIndex != -1) {
                    String lineKey = line.substring(0, equalsIndex).trim();
                    if (lineKey.equals(key)) {
                        // Update the line with new value
                        // Check if value should be quoted
                        String newValue;
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            newValue = value;
                        } else {
                            newValue = "\"" + value + "\"";
                        }
                        lines.set(i, key + " = " + newValue);
                        keyFound = true;
                        break;
                    }
                }
            }
            
            if (!keyFound) {
                System.out.println("Key not found in config file: " + key);
                return;
            }
            
            // Write the updated content back to the file
            Files.write(configPath, lines, StandardCharsets.UTF_8);
            
        } catch (IOException e) {
            System.err.println("Failed to modify config file: " + e.getMessage());
        }
    }

    @Override
    public void batchChangeConfig(List<Config> configs) {
        if (configs == null || configs.isEmpty()) {
            System.out.println("No configs to change");
            return;
        }
        
        progressBarComponent.start("Batch Change Config", configs.size());
        
        for (Config config : configs) {
            changeConfig(config);
            progressBarComponent.update("修改配置：" + config.getConfigFile() + " [" + config.getKey() + " = " + config.getValue() + "]");
        }
        
        progressBarComponent.finish();
    }
}

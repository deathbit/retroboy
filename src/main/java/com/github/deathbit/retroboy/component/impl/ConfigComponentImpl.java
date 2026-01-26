package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.domain.Config;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ConfigComponentImpl implements ConfigComponent {

    @Override
    public void changeConfig(Config config) throws IOException {
        String configFile = config.getFile();
        String key = config.getKey();
        String value = config.getValue();
        String fileName = config.getFile().substring(config.getFile().lastIndexOf("\\") + 1);

        Path configPath = Paths.get(configFile);
        List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }
            int equalsIndex = line.indexOf('=');
            if (equalsIndex != -1) {
                String lineKey = line.substring(0, equalsIndex).trim();
                if (lineKey.equals(key)) {
                    String newValue;
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        newValue = value;
                    } else {
                        newValue = "\"" + value + "\"";
                    }
                    lines.set(i, key + " = " + newValue);
                    break;
                }
            }
        }
        String content = String.join("\n", lines) + "\n";
        Files.writeString(configPath, content, StandardCharsets.UTF_8);
        System.out.println("设置选项[" + fileName + "]: " + key + " = " + value);
    }

    @Override
    public void batchChangeConfig(List<Config> configs) throws IOException {
        for (Config config : configs) {
            changeConfig(config);
        }
    }
}

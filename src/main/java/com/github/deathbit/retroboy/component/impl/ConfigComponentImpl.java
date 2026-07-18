package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.ConfigPair;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ConfigComponentImpl implements ConfigComponent {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void changeRetroArchConfig(ConfigPair configPair) throws Exception {
        ProgressBar pb = new ProgressBar("设置选项");
        Path configPath = Paths.get(appConfig.getGlobalConfig().getRaConfig());
        List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
        String key = configPair.getKey();
        String value = configPair.getValue();
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
        pb.done();
    }
}

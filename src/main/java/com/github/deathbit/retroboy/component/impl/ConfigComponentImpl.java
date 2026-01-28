package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.ConfigInput;
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
    public void batchChangeRaConfigs(List<ConfigInput> configInputs) throws Exception {
        ProgressBar pb = new ProgressBar("设置选项");
        pb.startTask(configInputs.size());
        Path configPath = Paths.get(appConfig.getGlobalConfig().getRaConfig());
        List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
        for (int i = 0; i < configInputs.size(); i++) {
            ConfigInput configInput = configInputs.get(i);
            String key = configInput.getKey();
            String value = configInput.getValue();
            for (int j = 0; j < lines.size(); j++) {
                String line = lines.get(j);
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
                        lines.set(j, key + " = " + newValue);
                        break;
                    }
                }
            }
            pb.updateTask(i);
        }
        String content = String.join("\n", lines) + "\n";
        Files.writeString(configPath, content, StandardCharsets.UTF_8);
        pb.finishTaskAndClose();
    }
}

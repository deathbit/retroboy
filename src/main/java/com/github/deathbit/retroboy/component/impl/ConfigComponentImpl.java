package com.github.deathbit.retroboy.component.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.github.deathbit.retroboy.component.ConfigComponent;
import com.github.deathbit.retroboy.component.domain.ConfigInput;
import com.github.deathbit.retroboy.domain.ProgressBar;
import org.springframework.stereotype.Component;

@Component
public class ConfigComponentImpl implements ConfigComponent {

    @Override
    public void batchChangeConfigs(List<ConfigInput> configInputs) throws Exception {
        ProgressBar pb = new ProgressBar("设置选项");
        pb.startTask(configInputs.size());
        for (int i = 0; i < configInputs.size(); i++) {
            ConfigInput configInput = configInputs.get(i);
            Path configPath = configInput.getFile();
            String key = configInput.getKey();
            String value = configInput.getValue();
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
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
            String content = String.join("\n", lines) + "\n";
            Files.writeString(configPath, content, StandardCharsets.UTF_8);
            pb.updateTask(i);
        }
        pb.finishTaskAndClose();
    }
}

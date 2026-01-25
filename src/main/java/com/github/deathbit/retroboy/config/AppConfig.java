package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.config.domain.Cleanup;
import com.github.deathbit.retroboy.config.domain.ConfigDefault;
import com.github.deathbit.retroboy.config.domain.CopyDefault;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.config")
public class AppConfig {
    private Cleanup cleanup;
    private CopyDefault copyDefault;
    private ConfigDefault configDefault;
    private String retroArchConfig;
}

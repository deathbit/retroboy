package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.config.domain.Cleanup;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.config")
public class AppConfig {
    private Cleanup cleanup;
}

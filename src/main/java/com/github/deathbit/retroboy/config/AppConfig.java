package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.config.basepacktask.*;
import com.github.deathbit.retroboy.config.tasks.CleanUpTask;
import com.github.deathbit.retroboy.config.tasks.DefaultConfigTask;
import com.github.deathbit.retroboy.config.tasks.FixChineseFontTask;
import com.github.deathbit.retroboy.config.tasks.SetMegaBezelShaderTask;
import com.github.deathbit.retroboy.domain.RuleConfig;
import com.github.deathbit.retroboy.enums.Platform;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.config")
public class AppConfig {
    private DeleteAllTaskConfig deleteAllTaskConfig;
    private SetUpESDEBaseTaskConfig setUpESDEBaseTaskConfig;
    private SetUpESDEUpdateTaskConfig setUpESDEUpdateTaskConfig;
    private SetUpRetroArchBaseTaskConfig setUpRetroArchBaseTaskConfig;
    private SetUpRetroArchUpdateTaskConfig setUpRetroArchUpdateTaskConfig;
    private SetUpRetroArchDefaultConfigTaskConfig setUpRetroArchDefaultConfigTaskConfig;
    private SetUpRetroArchFixChineseFontTaskConfig setUpRetroArchFixChineseFontTaskConfig;
    private SetUpRetroArchMegaBezelShaderTaskConfig setUpRetroArchMegaBezelShaderTaskConfig;
    private BasePackReleaseTaskConfig basePackReleaseTaskConfig;
    private GlobalConfig globalConfig;
    private CleanUpTask cleanUpTask;
    private DefaultConfigTask defaultConfigTask;
    private FixChineseFontTask fixChineseFontTask;
    private SetMegaBezelShaderTask setMegaBezelShaderTask;
    private Map<Platform, RuleConfig> ruleConfigMap;
}

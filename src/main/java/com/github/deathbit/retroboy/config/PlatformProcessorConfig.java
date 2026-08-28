package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class PlatformProcessorConfig {

    @Bean
    public Map<Platform, PlatformProcessor> platformProcessorMap(List<PlatformProcessor> processors) {
        return processors.stream()
                         .collect(Collectors.toMap(PlatformProcessor::platform, Function.identity()));
    }
}

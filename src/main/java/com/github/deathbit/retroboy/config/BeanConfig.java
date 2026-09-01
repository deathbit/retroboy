package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.wiki.WikiParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class BeanConfig {

    @Bean
    public Map<Platform, WikiParser> WikiParserMap(List<WikiParser> wikiParsers) {
        return wikiParsers.stream()
                         .collect(Collectors.toMap(WikiParser::getPlatform, Function.identity()));
    }
}

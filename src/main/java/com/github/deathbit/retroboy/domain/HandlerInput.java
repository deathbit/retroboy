package com.github.deathbit.retroboy.domain;

import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HandlerInput {
    private AppConfig appConfig;
    private FileComponent fileComponent;
}

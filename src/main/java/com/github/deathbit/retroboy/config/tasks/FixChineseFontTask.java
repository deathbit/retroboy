package com.github.deathbit.retroboy.config.tasks;

import com.github.deathbit.retroboy.domain.Config;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FixChineseFontTask {
    private String deleteFontFile;
    private CopyFileInput copyFontFile;
    private Config setNotificationFont;
}

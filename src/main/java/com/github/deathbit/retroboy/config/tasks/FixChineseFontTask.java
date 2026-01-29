package com.github.deathbit.retroboy.config.tasks;

import com.github.deathbit.retroboy.domain.ConfigInput;
import com.github.deathbit.retroboy.domain.CopyFileInput;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FixChineseFontTask {
    private String deleteOriginalFontFile;
    private CopyFileInput copyNewFontFile;
    private ConfigInput setNotificationFont;
}

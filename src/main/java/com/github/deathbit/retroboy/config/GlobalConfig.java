package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.domain.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfig {
    private boolean enableBase;
    private boolean enablePlatform;
    private String esdeHome;
    private String raHome;
    private String resHome;
    private String repo;
    private Author author;
    private String esdeVersion;
    private String raVersion;
    private String baiduPan;
    private String qqGroup;
    private String feedbackEmail;
}

package com.github.deathbit.retroboy.domain.screenscraper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadCompanyMediaInput {
    private Integer companyId;
    private String media;
    private String crc;
    private String md5;
    private String sha1;
    private Integer maxWidth;
    private Integer maxHeight;
}

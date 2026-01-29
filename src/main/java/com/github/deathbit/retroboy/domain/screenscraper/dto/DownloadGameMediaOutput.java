package com.github.deathbit.retroboy.domain.screenscraper.dto;

import com.github.deathbit.retroboy.domain.screenscraper.ApiResponseHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadGameMediaOutput {
    private ApiResponseHeader header;
    private byte[] data;
}

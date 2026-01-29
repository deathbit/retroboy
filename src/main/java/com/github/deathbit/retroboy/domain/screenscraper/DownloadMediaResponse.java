package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for download media (byte data)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadMediaResponse {
    private byte[] data;
}

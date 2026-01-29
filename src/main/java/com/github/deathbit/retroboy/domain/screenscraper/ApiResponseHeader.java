package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the header information returned in all ScreenScraper API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseHeader {
    /**
     * API version (e.g., "2.0")
     */
    private String apiVersion;

    /**
     * Date and time of the response (e.g., "2026-01-29 09:13:44")
     */
    private String dateTime;

    /**
     * The API command that was requested
     */
    private String commandRequested;

    /**
     * Whether the request was successful ("true" or "false")
     */
    private String success;

    /**
     * Error message if request failed (empty string if successful)
     */
    private String error;
}

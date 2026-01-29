package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API credentials for ScreenScraper authentication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredentials {
    /**
     * Developer ID provided by ScreenScraper
     */
    private String devId;
    
    /**
     * Developer password provided by ScreenScraper
     */
    private String devPassword;
    
    /**
     * Name of the calling software
     */
    private String softName;
    
    /**
     * ScreenScraper user ID (optional)
     */
    private String ssId;
    
    /**
     * ScreenScraper user password (optional)
     */
    private String ssPassword;
}

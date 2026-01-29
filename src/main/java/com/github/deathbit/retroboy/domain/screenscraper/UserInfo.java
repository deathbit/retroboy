package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ScreenScraper user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    /**
     * User's nickname on ScreenScraper
     */
    private String id;
    
    /**
     * User's numeric identifier on ScreenScraper
     */
    private Long numId;
    
    /**
     * User's level on ScreenScraper
     */
    private Integer level;
    
    /**
     * Financial contribution level on ScreenScraper (2 = 1 Additional Thread / 3+ = 5 Additional Threads)
     */
    private Integer contribution;
    
    /**
     * Counter of valid contributions (system media) submitted by the user
     */
    private Integer uploadSysteme;
    
    /**
     * Counter of valid contributions (text info) submitted by the user
     */
    private Integer uploadInfos;
    
    /**
     * Counter of valid contributions (ROM association) submitted by the user
     */
    private Integer romAsso;
    
    /**
     * Counter of valid contributions (game media) submitted by the user
     */
    private Integer uploadMedia;
    
    /**
     * Number of user proposals validated by a moderator
     */
    private Integer propositionOk;
    
    /**
     * Number of user proposals rejected by a moderator
     */
    private Integer propositionKo;
    
    /**
     * Percentage of user proposal rejections
     */
    private Double quotaRefu;
    
    /**
     * Number of threads allowed for the user
     */
    private Integer maxThreads;
    
    /**
     * Download speed (in KB/s) allowed for the user
     */
    private Integer maxDownloadSpeed;
    
    /**
     * Total number of API calls during the current day
     */
    private Integer requestsToday;
    
    /**
     * Number of API calls with a negative response during the current day
     */
    private Integer requestsKoToday;
    
    /**
     * Maximum number of API calls allowed per minute for the user
     */
    private Integer maxRequestsPerMin;
    
    /**
     * Maximum number of API calls allowed per day for the user
     */
    private Integer maxRequestsPerDay;
    
    /**
     * Maximum number of API calls with a negative response allowed per day for the user
     */
    private Integer maxRequestsKoPerDay;
    
    /**
     * Number of visits by the user to ScreenScraper
     */
    private Integer visits;
    
    /**
     * Date of the user's last visit to ScreenScraper (format: yyyy-mm-dd hh:mm:ss)
     */
    private String lastVisitDate;
    
    /**
     * Favorite region for visits (France, Europe, USA, Japan)
     */
    private String favRegion;
}

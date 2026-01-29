package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ScreenScraper server infrastructure information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo {
    /**
     * CPU usage percentage of server 1 (average of the last 5 minutes)
     */
    private Double cpu1;

    /**
     * CPU usage percentage of server 2 (average of the last 5 minutes)
     */
    private Double cpu2;

    /**
     * CPU usage percentage of server 3 (average of the last 5 minutes)
     */
    private Double cpu3;

    /**
     * Number of API accesses since the last minute
     */
    private Integer threadsMin;

    /**
     * Number of scrapers using the API since the last minute
     */
    private Integer nbScrapeurs;

    /**
     * Number of API accesses during the current day (GMT+1)
     */
    private Integer apiAcces;

    /**
     * API closed for anonymous users (0: open / 1: closed)
     */
    private Integer closeForNoMember;

    /**
     * API closed for non-participating members (0: open / 1: closed)
     */
    private Integer closeForLeecher;

    /**
     * Maximum number of threads open for anonymous users at the same time
     */
    private Integer maxThreadForNonMember;

    /**
     * Current number of threads opened simultaneously by anonymous users
     */
    private Integer threadForNonMember;

    /**
     * Maximum number of threads opened simultaneously by members
     */
    private Integer maxThreadForMember;

    /**
     * Current number of threads opened simultaneously by members
     */
    private Integer threadForMember;
}

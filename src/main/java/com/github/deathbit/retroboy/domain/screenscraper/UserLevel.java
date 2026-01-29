package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User level information on ScreenScraper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevel {
    /**
     * Numeric identifier of the level
     */
    private Integer id;

    /**
     * Name of the level in French
     */
    private String nomFr;
}

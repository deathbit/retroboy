package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Game family information with media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Family {
    /**
     * Numeric identifier of the family
     */
    private Integer id;

    /**
     * Name of the family
     */
    private String name;

    /**
     * Media URLs for the family (monochrome/color pictograms, backgrounds, figurines)
     */
    private Map<String, String> medias;
}

package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System media type information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMediaInfo {
    /**
     * Numeric identifier of the media
     */
    private Integer id;

    /**
     * Short name of the media
     */
    private String shortName;

    /**
     * Long name of the media
     */
    private String name;

    /**
     * Category of the media
     */
    private String category;

    /**
     * List of system types where the media is present (separated by |, empty = all types)
     */
    private String platformTypes;

    /**
     * List of systems where the media is present (separated by |, empty = all systems)
     */
    private String platforms;

    /**
     * Type of media
     */
    private String type;

    /**
     * File format of the media
     */
    private String fileFormat;

    /**
     * Second file format accepted
     */
    private String fileFormat2;

    /**
     * Auto-generated media (0=no, 1=yes)
     */
    private Integer autoGen;

    /**
     * Multi-region media (0=no, 1=yes)
     */
    private Integer multiRegions;

    /**
     * Multi-platform media (0=no, 1=yes)
     */
    private Integer multiPlatforms;

    /**
     * Multi-version media (0=no, 1=yes)
     */
    private Integer multiVersions;

    /**
     * Additional information about the media
     */
    private String extraInfosTxt;
}

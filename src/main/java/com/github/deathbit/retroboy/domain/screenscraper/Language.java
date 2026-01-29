package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Language information with multilingual names and media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Language {
    /**
     * Numeric identifier of the language
     */
    private Integer id;

    /**
     * Short name of the language
     */
    private String shortName;

    /**
     * Name of the language in German
     */
    private String nameDe;

    /**
     * Name of the language in English
     */
    private String nameEn;

    /**
     * Name of the language in Spanish
     */
    private String nameEs;

    /**
     * Name of the language in French
     */
    private String nameFr;

    /**
     * Name of the language in Italian
     */
    private String nameIt;

    /**
     * Name of the language in Portuguese
     */
    private String namePt;

    /**
     * ID of the parent language (0 if primary)
     */
    private Integer parent;

    /**
     * Media URLs for the language (monochrome/color pictograms, backgrounds)
     */
    private Map<String, String> medias;
}

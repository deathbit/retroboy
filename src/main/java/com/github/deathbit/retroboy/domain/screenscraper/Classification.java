package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Game classification/rating information with multilingual names and media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Classification {
    /**
     * Numeric identifier of the classification
     */
    private Integer id;
    
    /**
     * Short name of the classification
     */
    private String shortName;
    
    /**
     * Name of the classification in German
     */
    private String nameDe;
    
    /**
     * Name of the classification in English
     */
    private String nameEn;
    
    /**
     * Name of the classification in Spanish
     */
    private String nameEs;
    
    /**
     * Name of the classification in French
     */
    private String nameFr;
    
    /**
     * Name of the classification in Italian
     */
    private String nameIt;
    
    /**
     * Name of the classification in Portuguese
     */
    private String namePt;
    
    /**
     * ID of the parent classification (0 if primary)
     */
    private Integer parent;
    
    /**
     * Media URLs for the classification (monochrome/color pictograms, backgrounds)
     */
    private Map<String, String> medias;
}

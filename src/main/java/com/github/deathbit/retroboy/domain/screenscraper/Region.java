package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Region information with multilingual names and media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    /**
     * Numeric identifier of the region
     */
    private Integer id;
    
    /**
     * Short name of the region
     */
    private String shortName;
    
    /**
     * Name of the region in German
     */
    private String nameDe;
    
    /**
     * Name of the region in English
     */
    private String nameEn;
    
    /**
     * Name of the region in Spanish
     */
    private String nameEs;
    
    /**
     * Name of the region in French
     */
    private String nameFr;
    
    /**
     * Name of the region in Italian
     */
    private String nameIt;
    
    /**
     * Name of the region in Portuguese
     */
    private String namePt;
    
    /**
     * ID of the parent region (0 if primary)
     */
    private Integer parent;
    
    /**
     * Media URLs for the region (monochrome/color pictograms, backgrounds)
     */
    private Map<String, String> medias;
}

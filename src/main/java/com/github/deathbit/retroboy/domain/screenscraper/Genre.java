package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Game genre information with multilingual names and media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    /**
     * Numeric identifier of the genre
     */
    private Integer id;
    
    /**
     * Name of the genre in German
     */
    private String nameDe;
    
    /**
     * Name of the genre in English
     */
    private String nameEn;
    
    /**
     * Name of the genre in Spanish
     */
    private String nameEs;
    
    /**
     * Name of the genre in French
     */
    private String nameFr;
    
    /**
     * Name of the genre in Italian
     */
    private String nameIt;
    
    /**
     * Name of the genre in Portuguese
     */
    private String namePt;
    
    /**
     * ID of the parent genre (0 if main genre)
     */
    private Integer parent;
    
    /**
     * Media URLs for the genre (monochrome/color pictograms, backgrounds)
     */
    private Map<String, String> medias;
}

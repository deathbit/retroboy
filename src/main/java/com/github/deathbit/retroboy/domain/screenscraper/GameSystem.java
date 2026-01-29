package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Gaming system information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSystem {
    /**
     * Numeric identifier of the system
     */
    private Integer id;
    
    /**
     * Numeric identifier of the parent system
     */
    private Integer parentId;
    
    /**
     * System names in different regions and front-ends
     */
    private Map<String, String> names;
    
    /**
     * Extensions of usable ROM files
     */
    private String extensions;
    
    /**
     * Name of the system's production company
     */
    private String company;
    
    /**
     * System type (Arcade, Console, Portable Console, etc.)
     */
    private String type;
    
    /**
     * Year production began
     */
    private String startDate;
    
    /**
     * Year production ended
     */
    private String endDate;
    
    /**
     * ROM type(s)
     */
    private String romType;
    
    /**
     * Type of the original system media
     */
    private String supportType;
    
    /**
     * Media URLs for the system (logos, wheels, photos, videos, bezels, etc.)
     */
    private Map<String, Object> medias;
}

package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Game information type metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfo {
    /**
     * Numeric identifier of the info
     */
    private Integer id;
    
    /**
     * Short name of the info
     */
    private String shortName;
    
    /**
     * Long name of the info
     */
    private String name;
    
    /**
     * Category of the info
     */
    private String category;
    
    /**
     * List of system types where the info is present (separated by |, empty = all types)
     */
    private String platformTypes;
    
    /**
     * List of systems where the info is present (separated by |, empty = all systems)
     */
    private String platforms;
    
    /**
     * Type of info
     */
    private String type;
    
    /**
     * Auto-generated info (0=no, 1=yes)
     */
    private Integer autoGen;
    
    /**
     * Multi-region info (0=no, 1=yes)
     */
    private Integer multiRegions;
    
    /**
     * Multi-support info (0=no, 1=yes)
     */
    private Integer multiSupports;
    
    /**
     * Multi-version info (0=no, 1=yes)
     */
    private Integer multiVersions;
    
    /**
     * Multi-choice info (0=no, 1=yes)
     */
    private Integer multiChoice;
}

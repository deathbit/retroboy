package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Complete game information including metadata and media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    /**
     * Numeric identifier of the game
     */
    private Integer id;
    
    /**
     * Numeric identifier of the ROM
     */
    private Integer romId;
    
    /**
     * Indicates whether the ROM is a game or non-game (demo/app)
     */
    private Boolean notGame;
    
    /**
     * Name of the game (internal ScreenScraper)
     */
    private String name;
    
    /**
     * Game names in different regions
     */
    private Map<String, String> names;
    
    /**
     * Short names of ROM regions
     */
    private List<String> regionShortNames;
    
    /**
     * ID of the clone (if available)
     */
    private Integer cloneOf;
    
    /**
     * System information
     */
    private GameSystem system;
    
    /**
     * Name of the editor
     */
    private String editor;
    
    /**
     * Media for the editor
     */
    private Map<String, String> mediaEditor;
    
    /**
     * Name of the developer
     */
    private String developer;
    
    /**
     * Media for the developer
     */
    private Map<String, String> mediaDeveloper;
    
    /**
     * Number of players
     */
    private String players;
    
    /**
     * Score out of 20
     */
    private Double score;
    
    /**
     * Game included in TOP Staff ScreenScraper (0: not included, 1: included)
     */
    private Integer topStaff;
    
    /**
     * Game screen rotation (only for arcade games)
     */
    private String rotation;
    
    /**
     * Game resolution (only for arcade games)
     */
    private String resolution;
    
    /**
     * Game synopsis in different languages
     */
    private Map<String, String> synopsis;
    
    /**
     * Game classifications
     */
    private Map<String, Object> classifications;
    
    /**
     * Release dates in different regions
     */
    private Map<String, String> dates;
    
    /**
     * Genre information
     */
    private Map<String, Object> genres;
    
    /**
     * Game mode information
     */
    private Map<String, Object> modes;
    
    /**
     * Family information
     */
    private Map<String, Object> families;
    
    /**
     * Theme information
     */
    private Map<String, Object> themes;
    
    /**
     * Style information
     */
    private Map<String, Object> styles;
    
    /**
     * Media URLs for the game (screenshots, fanart, videos, logos, boxes, manuals, etc.)
     */
    private Map<String, Object> medias;
    
    /**
     * List of known ROMs associated with the game
     */
    private List<Rom> roms;
    
    /**
     * Information about the scraped ROM
     */
    private Rom rom;
}

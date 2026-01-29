package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ROM (Read-Only Memory) file information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rom {
    /**
     * Numeric identifier of the ROM
     */
    private Integer id;

    /**
     * Media number (e.g., 1 = floppy disk 01 or CD 01)
     */
    private Integer romNumSupport;

    /**
     * Total number of media (e.g., 2 = 2 floppy disks or 2 CDs)
     */
    private Integer romTotalSupport;

    /**
     * Name of the ROM file or folder
     */
    private String romFileName;

    /**
     * Manufacturer's serial number
     */
    private String romSerial;

    /**
     * Region(s) of the ROM (e.g., "fr,us,sp")
     */
    private String romRegions;

    /**
     * Language(s) of the ROM (e.g., "fr,en,es")
     */
    private String romLangues;

    /**
     * ROM type
     */
    private String romType;

    /**
     * Media type
     */
    private String romSupportType;

    /**
     * Size in bytes of the ROM file or folder contents
     */
    private Long romSize;

    /**
     * CRC32 hash of the ROM file
     */
    private String romCrc;

    /**
     * MD5 hash of the ROM file
     */
    private String romMd5;

    /**
     * SHA1 hash of the ROM file
     */
    private String romSha1;

    /**
     * Numeric identifier of the parent ROM if this is a clone
     */
    private Integer romCloneOf;

    /**
     * Beta version of the game (0=no, 1=yes)
     */
    private Integer beta;

    /**
     * Demo version of the game (0=no, 1=yes)
     */
    private Integer demo;

    /**
     * Translated version of the game (0=no, 1=yes)
     */
    private Integer trad;

    /**
     * Modified version of the game (0=no, 1=yes)
     */
    private Integer hack;

    /**
     * Unofficial game (0=no, 1=yes)
     */
    private Integer unl;

    /**
     * Alternative version of the game (0=no, 1=yes)
     */
    private Integer alt;

    /**
     * Best version of the game (0=no, 1=yes)
     */
    private Integer best;

    /**
     * Netplay compatible (0=no, 1=yes)
     */
    private Integer netplay;

    /**
     * Gamelink compatible (0=no, 1=yes)
     */
    private Integer gamelink;

    /**
     * Total number of times scraped
     */
    private Integer nbScrap;

    /**
     * Number of players specific to the ROM
     */
    private String players;

    /**
     * Release dates specific to the ROM in different regions
     */
    private Map<String, String> dates;

    /**
     * Publisher name specific to the ROM
     */
    private String publisher;

    /**
     * Developer name specific to the ROM
     */
    private String developer;

    /**
     * Description specific to the ROM in different languages
     */
    private Map<String, String> synopsis;

    /**
     * Clone types
     */
    private Map<String, Object> cloneTypes;

    /**
     * Hack types
     */
    private Map<String, Object> hackTypes;
}

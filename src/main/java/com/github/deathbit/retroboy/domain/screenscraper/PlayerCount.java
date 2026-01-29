package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Number of players information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCount {
    /**
     * Numeric identifier of the number of players
     */
    private Integer id;

    /**
     * Designation of the number of players
     */
    private String name;

    /**
     * Numeric identifier of the parent (0 if no parent)
     */
    private Integer parent;
}

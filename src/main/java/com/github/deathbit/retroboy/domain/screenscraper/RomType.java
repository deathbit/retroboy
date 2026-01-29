package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ROM type information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RomType {
    /**
     * Designation of the ROM type(s)
     */
    private String name;
}

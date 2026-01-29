package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Media support type information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportType {
    /**
     * Designation of the support(s)
     */
    private String name;
}

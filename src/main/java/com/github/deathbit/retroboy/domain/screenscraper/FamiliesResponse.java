package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for families
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamiliesResponse {
    private List<Family> familles;
}

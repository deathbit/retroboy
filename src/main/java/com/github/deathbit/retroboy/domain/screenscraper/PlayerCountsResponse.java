package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for player counts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCountsResponse {
    private List<PlayerCount> joueurs;
}

package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for game info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoResponse {
    private Game jeu;
}

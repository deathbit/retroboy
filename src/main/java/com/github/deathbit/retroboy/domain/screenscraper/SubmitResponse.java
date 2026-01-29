package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for submit operations (rating, proposal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResponse {
    private String message;
}

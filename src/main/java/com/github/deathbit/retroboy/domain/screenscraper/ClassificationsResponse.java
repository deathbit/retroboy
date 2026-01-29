package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for classifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationsResponse {
    private List<Classification> classifications;
}

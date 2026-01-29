package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for infrastructure info containing server information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructureInfoResponse {
    private ServerInfo serveurs;
}

package com.github.deathbit.retroboy.domain.screenscraper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitProposalInput {
    private Integer gameId;
    private Integer romId;
    private Map<String, Object> proposalData;
}

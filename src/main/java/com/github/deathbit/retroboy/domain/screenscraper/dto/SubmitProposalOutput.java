package com.github.deathbit.retroboy.domain.screenscraper.dto;

import com.github.deathbit.retroboy.domain.screenscraper.ApiResponseHeader;
import com.github.deathbit.retroboy.domain.screenscraper.SubmitResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitProposalOutput {
    private ApiResponseHeader header;
    private SubmitResponse response;
}

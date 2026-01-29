package com.github.deathbit.retroboy.domain.screenscraper.dto;

import com.github.deathbit.retroboy.domain.screenscraper.ApiResponseHeader;
import com.github.deathbit.retroboy.domain.screenscraper.SystemListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSystemListOutput {
    private ApiResponseHeader header;
    private SystemListResponse response;
}

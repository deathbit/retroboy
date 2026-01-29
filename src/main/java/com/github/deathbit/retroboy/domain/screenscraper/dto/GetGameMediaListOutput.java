package com.github.deathbit.retroboy.domain.screenscraper.dto;

import com.github.deathbit.retroboy.domain.screenscraper.ApiResponseHeader;
import com.github.deathbit.retroboy.domain.screenscraper.GameMediaInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGameMediaListOutput {
    private ApiResponseHeader header;
    private List<GameMediaInfo> gameMediaList;
}

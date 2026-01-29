package com.github.deathbit.retroboy.domain.screenscraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for ROM info list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RomInfoListResponse {
    private List<RomInfo> rominfos;
}

package com.github.deathbit.retroboy.domain.screenscraper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGameInfoInput {
    private Integer systemId;
    private String crc;
    private String md5;
    private String sha1;
    private String romType;
    private String romName;
    private Long romSize;
    private Integer gameId;
}

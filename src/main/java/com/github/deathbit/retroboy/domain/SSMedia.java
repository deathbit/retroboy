package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSMedia {
    private String type;
    private String parent;
    private String url;
    private String region;
    private String crc;
    private String md5;
    private String sha1;
    private String size;
    private String format;
}

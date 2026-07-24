package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorInfo {
    private String name;
    private String wechat;
    private String qq;
    private String tiktok;
    private String xhs;
    private String bilibili;
    private String ks;
}

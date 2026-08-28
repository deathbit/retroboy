package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiDBPackage {
    private String id;
    private Map<String, WikiDB> wikiDBByArea;
    private Map<String, String> matchNameByArea;
}

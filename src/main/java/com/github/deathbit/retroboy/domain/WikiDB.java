package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiDB {
    private String id;
    private String packageId;
    private String name;
    private String developer;
    private String publisher;
    private String firstReleased;
    private String releaseDate;
    private String matchName;
    private String area;
}

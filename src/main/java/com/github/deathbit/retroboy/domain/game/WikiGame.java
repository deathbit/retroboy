package com.github.deathbit.retroboy.domain.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiGame implements Game {
    private String id;
    private String packageId;
    private String title;
    private String area;
    private String developer;
    private String publisher;
    private String releaseDate;
}

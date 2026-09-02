package com.github.deathbit.retroboy.domain.gamepackage;

import com.github.deathbit.retroboy.domain.game.SSGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSGamePackage implements GamePackage {
    private String id;
    private Map<String, SSGame> ssGameByArea;
    private String developer;
    private String publisher;
    private String description;
    private String genre;
    private String player;
    private List<String> sha1s;
}

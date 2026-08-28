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
public class GameDBPackage {
    private String id;
    private Map<String, GameDB> gameDBByArea;
    private Map<String, String> matchNameByArea;
}

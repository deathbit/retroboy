package com.github.deathbit.retroboy.processor;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.domain.WikiDBPackage;
import com.github.deathbit.retroboy.enums.Platform;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PlatformProcessor {

    Platform platform();

    default void processGameDB(Map<String, GameDB> gameDBMapByRomName) {}

    default List<WikiDBPackage> processWiki() throws Exception {
        return List.of();
    }

    /**
     * 返回 GameDB 地区到 WikiDB 地区的映射关系。
     * 默认规则：JPN → JPN，USA → USA，其余地区 → PAL。
     */
    default Map<String, String> resolveAreaMapping(List<String> areas) {
        return areas.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            area -> switch (area) {
                                case "JPN" -> "JPN";
                                case "USA" -> "USA";
                                default -> "PAL";
                            }));
    }
}

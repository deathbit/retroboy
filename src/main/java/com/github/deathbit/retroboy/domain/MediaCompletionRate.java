package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaCompletionRate {
    private int totalCount;
    private int completedCount;
    private double completionRate;

    public static MediaCompletionRate of(int totalCount, int completedCount) {
        return MediaCompletionRate.builder()
                .totalCount(totalCount)
                .completedCount(completedCount)
                .completionRate(totalCount == 0 ? 0 : (double) completedCount / totalCount)
                .build();
    }
}

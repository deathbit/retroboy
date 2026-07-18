package com.github.deathbit.retroboy.config.basepacktask;

import com.github.deathbit.retroboy.domain.ConfigPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetUpRetroArchDefaultConfigTaskConfig {
    private String taskName;
    private boolean enabled;
    private List<ConfigPair> configPairs;
}

package com.github.deathbit.retroboy.config;

import com.github.deathbit.retroboy.domain.AreaConfig;
import com.github.deathbit.retroboy.domain.RenameOption;
import com.github.deathbit.retroboy.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformPackTaskConfig {
    private Platform platform;
    private boolean enabled;
    private List<AreaConfig> areaConfigs;
    private Set<String> tagBlackList;
    private Set<String> fileNameBlackList;
    private List<RenameOption> renameOptions;
}

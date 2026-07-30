package com.github.deathbit.retroboy.config.basepacktask;

import com.github.deathbit.retroboy.domain.ReleaseNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasePackReleaseReportTaskConfig {
    private String taskName;
    private boolean enabled;
    private Path targetPath;
    private String basePackVersion;
    private List<ReleaseNote> releaseNotes;
}

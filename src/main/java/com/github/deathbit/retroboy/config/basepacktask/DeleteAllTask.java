package com.github.deathbit.retroboy.config.basepacktask;

import com.github.deathbit.retroboy.config.BasePackTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAllTask implements BasePackTask {
    private String taskName;
    private boolean enabled;
    private String deleteDir;

    @Override
    public String taskName() {
        return taskName;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }
}

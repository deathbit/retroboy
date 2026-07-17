package com.github.deathbit.retroboy.component.impl.basepacktasks;

import com.github.deathbit.retroboy.component.BasePackTaskComponent;
import com.github.deathbit.retroboy.component.FileComponent;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.domain.RunnableWithException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteAllTaskComponent implements BasePackTaskComponent {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileComponent fileComponent;

    @Override
    public String name() {
        return appConfig.getDeleteAllTaskConfig().getName();
    }

    @Override
    public boolean enabled() {
        return appConfig.getDeleteAllTaskConfig().isEnabled();
    }

    @Override
    public RunnableWithException runnable() {
        return () -> {
            fileComponent.
        };
    }
}

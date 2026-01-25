package com.github.deathbit.retroboy.component.impl;

import com.github.deathbit.retroboy.component.CleanUpComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CleanUpComponentImpl implements CleanUpComponent {

    @Override
    public void deleteDir(String dir) {

    }

    @Override
    public void deleteFile(String file) {

    }
}

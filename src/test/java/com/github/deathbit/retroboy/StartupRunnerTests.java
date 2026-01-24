package com.github.deathbit.retroboy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StartupRunnerTests {

    @Autowired(required = false)
    private StartupRunner startupRunner;

    @Test
    void startupRunnerBeanShouldBeCreated() {
        assertThat(startupRunner).isNotNull();
    }
}

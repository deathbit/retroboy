package com.github.deathbit.retroboy.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartupTaskTest {

    @Test
    void shouldEnableOnlyListedTasks() {
        String startupTaskMask = "CLEAN_UP|DEFAULT_CONFIG|!FIX_CHINESE_FONT";

        assertThat(StartupTask.isEnabled(StartupTask.CLEAN_UP, startupTaskMask)).isTrue();
        assertThat(StartupTask.isEnabled(StartupTask.DEFAULT_CONFIG, startupTaskMask)).isTrue();
        assertThat(StartupTask.isEnabled(StartupTask.FIX_CHINESE_FONT, startupTaskMask)).isFalse();
        assertThat(StartupTask.isEnabled(StartupTask.SET_MEGA_BEZEL_SHADER, startupTaskMask)).isFalse();
        assertThat(StartupTask.isEnabled(StartupTask.SET_PLATFORM, startupTaskMask)).isFalse();
    }

    @Test
    void shouldSkipBlankConfiguration() {
        assertThat(StartupTask.isEnabled(StartupTask.CLEAN_UP, null)).isFalse();
        assertThat(StartupTask.isEnabled(StartupTask.CLEAN_UP, " ")).isFalse();
    }

    @Test
    void shouldKeepLegacyNumericMaskCompatibility() {
        assertThat(StartupTask.isEnabled(StartupTask.CLEAN_UP, "31")).isTrue();
        assertThat(StartupTask.isEnabled(StartupTask.SET_PLATFORM, "31")).isTrue();
        assertThat(StartupTask.isEnabled(StartupTask.CLEAN_UP, "0")).isFalse();
    }

    @Test
    void shouldLetExplicitlyDisabledTaskWin() {
        assertThat(StartupTask.isEnabled(StartupTask.CLEAN_UP, "CLEAN_UP|!CLEAN_UP")).isFalse();
    }

    @Test
    void shouldRejectUnknownTaskNames() {
        assertThatThrownBy(() -> StartupTask.isEnabled(StartupTask.CLEAN_UP, "CLEAN_UP|UNKNOWN_TASK"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown startup task: UNKNOWN_TASK. Valid tasks are: CLEAN_UP, DEFAULT_CONFIG, FIX_CHINESE_FONT, SET_MEGA_BEZEL_SHADER, SET_PLATFORM");
    }
}

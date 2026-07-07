package com.github.deathbit.retroboy.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformTest {

    @Test
    void shouldEnableNamedPlatforms() {
        String platformTaskMask = "NES|SNES|!MD";

        assertTrue(Platform.isEnabled(Platform.NES, platformTaskMask));
        assertTrue(Platform.isEnabled(Platform.SNES, platformTaskMask));
        assertFalse(Platform.isEnabled(Platform.MD, platformTaskMask));
    }

    @Test
    void shouldLetDisabledPlatformWin() {
        assertFalse(Platform.isEnabled(Platform.NES, "NES|!NES"));
    }

    @Test
    void shouldSkipBlankPlatformTaskMask() {
        assertFalse(Platform.isEnabled(Platform.NES, null));
        assertFalse(Platform.isEnabled(Platform.NES, " "));
    }

    @Test
    void shouldRejectUnknownPlatform() {
        assertThrows(IllegalArgumentException.class, () -> Platform.isEnabled(Platform.NES, "NES|UNKNOWN"));
    }
}

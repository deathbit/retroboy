package com.github.deathbit.retroboy.platform.impl;

import com.github.deathbit.retroboy.domain.gamepackage.SSGamePackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SSHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void readGamePackageKeepsPackageWithOnlyScreenScraperRegion() throws Exception {
        var inputPath = tempDir.resolve("only-ss-region.json");
        Files.writeString(inputPath, """
                {
                  "id": "100",
                  "noms": [
                    {
                      "region": "ss",
                      "text": "ScreenScraper Title"
                    }
                  ],
                  "roms": [
                    {
                      "romsha1": " abcdef "
                    }
                  ]
                }
                """, StandardCharsets.UTF_8);

        SSGamePackage ssGamePackage = ReflectionTestUtils.invokeMethod(new SSHandler(), "readGamePackage", inputPath);

        assertThat(ssGamePackage).isNotNull();
        assertThat(ssGamePackage.getId()).isEqualTo("100");
        assertThat(ssGamePackage.getSsGameByArea()).isEmpty();
        assertThat(ssGamePackage.getSha1s()).containsExactly("ABCDEF");
    }

    @Test
    void removeDuplicateSha1sRemovesSha1FromEveryPackageWhenItAppearsInMultiplePackages() {
        var firstPackage = SSGamePackage.builder()
                .id("100")
                .sha1s(new ArrayList<>(List.of("AAA", "DUPLICATE")))
                .build();
        var secondPackage = SSGamePackage.builder()
                .id("200")
                .sha1s(new ArrayList<>(List.of("BBB", "duplicate")))
                .build();
        var thirdPackage = SSGamePackage.builder()
                .id("300")
                .sha1s(new ArrayList<>(List.of("CCC")))
                .build();

        ReflectionTestUtils.invokeMethod(new SSHandler(), "removeDuplicateSha1s", List.of(firstPackage, secondPackage, thirdPackage));

        assertThat(firstPackage.getSha1s()).containsExactly("AAA");
        assertThat(secondPackage.getSha1s()).containsExactly("BBB");
        assertThat(thirdPackage.getSha1s()).containsExactly("CCC");
    }
}


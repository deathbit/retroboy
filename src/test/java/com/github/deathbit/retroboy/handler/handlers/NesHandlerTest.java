package com.github.deathbit.retroboy.handler.handlers;

import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.rule.domain.RuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NesHandlerTest {

    @Test
    void testBuildRuleContext_shouldExtractGameNamesFromDatFile() throws URISyntaxException {
        // Arrange
        NesHandler handler = new NesHandler();
        File datFile = new File(getClass().getClassLoader().getResource("test-nes.dat").toURI());
        RuleConfig ruleConfig = RuleConfig.builder()
                .datFile(datFile.getAbsolutePath())
                .build();

        // Act
        RuleContext result = handler.buildRuleContext(ruleConfig);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicensed());
        assertEquals(3, result.getLicensed().size());
        assertTrue(result.getLicensed().contains("'89 Dennou Kyuusei Uranai (Japan)"));
        assertTrue(result.getLicensed().contains("10-Yard Fight (Japan) (En)"));
        assertTrue(result.getLicensed().contains("Super Mario Bros. (World)"));
    }

    @Test
    void testBuildRuleContext_shouldHandleEmptyDatFile(@TempDir Path tempDir) throws IOException {
        // Arrange
        NesHandler handler = new NesHandler();
        File datFile = tempDir.resolve("empty.dat").toFile();
        Files.writeString(datFile.toPath(), 
                "<?xml version=\"1.0\"?>\n" +
                "<datafile>\n" +
                "  <header><name>Test</name></header>\n" +
                "</datafile>");
        
        RuleConfig ruleConfig = RuleConfig.builder()
                .datFile(datFile.getAbsolutePath())
                .build();

        // Act
        RuleContext result = handler.buildRuleContext(ruleConfig);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicensed());
        assertEquals(0, result.getLicensed().size());
    }

    @Test
    void testBuildRuleContext_shouldThrowExceptionForInvalidFile() {
        // Arrange
        NesHandler handler = new NesHandler();
        RuleConfig ruleConfig = RuleConfig.builder()
                .datFile("/nonexistent/file.dat")
                .build();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            handler.buildRuleContext(ruleConfig);
        });
        assertTrue(exception.getMessage().contains("Failed to parse DAT file"));
    }

    @Test
    void testBuildRuleContext_shouldIgnoreGamesWithoutNameAttribute(@TempDir Path tempDir) throws IOException {
        // Arrange
        NesHandler handler = new NesHandler();
        File datFile = tempDir.resolve("partial.dat").toFile();
        Files.writeString(datFile.toPath(), 
                "<?xml version=\"1.0\"?>\n" +
                "<datafile>\n" +
                "  <header><name>Test</name></header>\n" +
                "  <game name=\"Game 1\" id=\"1\"><description>Game 1</description></game>\n" +
                "  <game id=\"2\"><description>Game without name</description></game>\n" +
                "  <game name=\"\" id=\"3\"><description>Game with empty name</description></game>\n" +
                "  <game name=\"Game 2\" id=\"4\"><description>Game 2</description></game>\n" +
                "</datafile>");
        
        RuleConfig ruleConfig = RuleConfig.builder()
                .datFile(datFile.getAbsolutePath())
                .build();

        // Act
        RuleContext result = handler.buildRuleContext(ruleConfig);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLicensed());
        assertEquals(2, result.getLicensed().size());
        assertTrue(result.getLicensed().contains("Game 1"));
        assertTrue(result.getLicensed().contains("Game 2"));
    }
}

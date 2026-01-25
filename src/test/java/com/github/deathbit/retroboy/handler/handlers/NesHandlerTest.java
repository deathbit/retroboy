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

    @Test
    void testBuildFileContext_shouldParseSimpleFileName() {
        // Arrange
        NesHandler handler = new NesHandler();
        
        // Act
        var result = handler.buildFileContext("'89 Dennou Kyuusei Uranai (Japan).nes");
        
        // Assert
        assertNotNull(result);
        assertEquals("'89 Dennou Kyuusei Uranai (Japan).nes", result.getFileName());
        assertEquals("'89 Dennou Kyuusei Uranai (Japan)", result.getFullName());
        assertEquals("'89 Dennou Kyuusei Uranai", result.getNamePart());
        assertEquals("(Japan)", result.getTagPart());
        assertEquals(1, result.getTags().size());
        assertEquals("Japan", result.getTags().get(0));
    }

    @Test
    void testBuildFileContext_shouldParseFileNameWithMultipleTags() {
        // Arrange
        NesHandler handler = new NesHandler();
        
        // Act
        var result = handler.buildFileContext("10-Yard Fight (Japan) (En).nes");
        
        // Assert
        assertNotNull(result);
        assertEquals("10-Yard Fight (Japan) (En).nes", result.getFileName());
        assertEquals("10-Yard Fight (Japan) (En)", result.getFullName());
        assertEquals("10-Yard Fight", result.getNamePart());
        assertEquals("(Japan) (En)", result.getTagPart());
        assertEquals(2, result.getTags().size());
        assertEquals("Japan", result.getTags().get(0));
        assertEquals("En", result.getTags().get(1));
    }

    @Test
    void testBuildFileContext_shouldHandleFileNameWithoutTags() {
        // Arrange
        NesHandler handler = new NesHandler();
        
        // Act
        var result = handler.buildFileContext("Simple Game.nes");
        
        // Assert
        assertNotNull(result);
        assertEquals("Simple Game.nes", result.getFileName());
        assertEquals("Simple Game", result.getFullName());
        assertEquals("Simple Game", result.getNamePart());
        assertEquals("", result.getTagPart());
        assertEquals(0, result.getTags().size());
    }

    @Test
    void testBuildFileContext_shouldHandleFileNameWithoutExtension() {
        // Arrange
        NesHandler handler = new NesHandler();
        
        // Act
        var result = handler.buildFileContext("Game (World)");
        
        // Assert
        assertNotNull(result);
        assertEquals("Game (World)", result.getFileName());
        assertEquals("Game (World)", result.getFullName());
        assertEquals("Game", result.getNamePart());
        assertEquals("(World)", result.getTagPart());
        assertEquals(1, result.getTags().size());
        assertEquals("World", result.getTags().get(0));
    }
}

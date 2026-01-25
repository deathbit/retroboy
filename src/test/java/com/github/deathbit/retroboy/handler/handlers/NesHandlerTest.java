package com.github.deathbit.retroboy.handler.handlers;

import com.github.deathbit.retroboy.component.CopyComponent;
import com.github.deathbit.retroboy.component.CreateComponent;
import com.github.deathbit.retroboy.component.ProgressBarComponent;
import com.github.deathbit.retroboy.component.impl.CopyComponentImpl;
import com.github.deathbit.retroboy.component.impl.CreateComponentImpl;
import com.github.deathbit.retroboy.component.impl.ProgressBarComponentImpl;
import com.github.deathbit.retroboy.config.AppConfig;
import com.github.deathbit.retroboy.config.domain.RuleConfig;
import com.github.deathbit.retroboy.rule.domain.RuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    void testHandle_shouldCopyFilesToTargetDirectories(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Arrange
        NesHandler handler = new NesHandler();
        
        // Create test directory structure
        Path romDir = tempDir.resolve("roms");
        Path japanTargetDir = tempDir.resolve("japan");
        Path usaTargetDir = tempDir.resolve("usa");
        Path europeTargetDir = tempDir.resolve("europe");
        Files.createDirectories(romDir);
        
        // Create test DAT file with licensed games
        File datFile = tempDir.resolve("test.dat").toFile();
        Files.writeString(datFile.toPath(),
                "<?xml version=\"1.0\"?>\n" +
                "<datafile>\n" +
                "  <header><name>Test</name></header>\n" +
                "  <game name=\"'89 Dennou Kyuusei Uranai (Japan)\"><description>Game 1</description></game>\n" +
                "  <game name=\"10-Yard Fight (USA)\"><description>Game 2</description></game>\n" +
                "  <game name=\"Super Mario Bros. (Europe)\"><description>Game 3</description></game>\n" +
                "</datafile>");
        
        // Create test ROM files
        Path japanRom = romDir.resolve("'89 Dennou Kyuusei Uranai (Japan).nes");
        Path usaRom = romDir.resolve("10-Yard Fight (USA).nes");
        Path europeRom = romDir.resolve("Super Mario Bros. (Europe).nes");
        Files.writeString(japanRom, "Japan ROM content");
        Files.writeString(usaRom, "USA ROM content");
        Files.writeString(europeRom, "Europe ROM content");
        
        // Create RuleConfig
        RuleConfig ruleConfig = RuleConfig.builder()
                .datFile(datFile.getAbsolutePath())
                .romDir(romDir.toString())
                .japanTargetDir(japanTargetDir.toString())
                .usaTargetDir(usaTargetDir.toString())
                .europeTargetDir(europeTargetDir.toString())
                .build();
        
        // Create AppConfig and inject it
        AppConfig appConfig = new AppConfig();
        ReflectionTestUtils.setField(appConfig, "nesRuleConfig", ruleConfig);
        ReflectionTestUtils.setField(handler, "appConfig", appConfig);
        
        // Create and inject component dependencies
        ProgressBarComponent progressBarComponent = new ProgressBarComponentImpl();
        ReflectionTestUtils.setField(handler, "createComponent", new CreateComponentImpl(progressBarComponent));
        ReflectionTestUtils.setField(handler, "progressBarComponent", progressBarComponent);
        ReflectionTestUtils.setField(handler, "copyComponent", new CopyComponentImpl(progressBarComponent));
        
        // Act
        handler.handle();
        
        // Assert - Check that target directories were created
        assertTrue(Files.exists(japanTargetDir), "Japan target directory should be created");
        assertTrue(Files.exists(usaTargetDir), "USA target directory should be created");
        assertTrue(Files.exists(europeTargetDir), "Europe target directory should be created");
        
        // Assert - Check that files were copied to correct directories
        Path copiedJapanRom = japanTargetDir.resolve("'89 Dennou Kyuusei Uranai (Japan).nes");
        Path copiedUsaRom = usaTargetDir.resolve("10-Yard Fight (USA).nes");
        Path copiedEuropeRom = europeTargetDir.resolve("Super Mario Bros. (Europe).nes");
        
        assertTrue(Files.exists(copiedJapanRom), "Japan ROM should be copied");
        assertTrue(Files.exists(copiedUsaRom), "USA ROM should be copied");
        assertTrue(Files.exists(copiedEuropeRom), "Europe ROM should be copied");
        
        // Assert - Verify file contents
        assertEquals("Japan ROM content", Files.readString(copiedJapanRom));
        assertEquals("USA ROM content", Files.readString(copiedUsaRom));
        assertEquals("Europe ROM content", Files.readString(copiedEuropeRom));
    }
}

package com.github.deathbit.retroboy.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "cleanup.directory=${java.io.tmpdir}/test-cleanup"
})
class CleanUpComponentImplTests {

    @Autowired
    private CleanUpComponent cleanUpComponent;

    @Test
    void cleanUpComponentBeanShouldBeCreated() {
        assertThat(cleanUpComponent).isNotNull();
        assertThat(cleanUpComponent).isInstanceOf(CleanUpComponentImpl.class);
    }

    @Test
    void cleanShouldDeleteAllFilesInDirectory(@TempDir Path tempDir) throws Exception {
        // Create test files and directories
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path subDir = tempDir.resolve("subdir");
        Path file3 = subDir.resolve("file3.txt");
        
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createDirectory(subDir);
        Files.createFile(file3);

        // Verify files exist
        assertThat(Files.exists(file1)).isTrue();
        assertThat(Files.exists(file2)).isTrue();
        assertThat(Files.exists(subDir)).isTrue();
        assertThat(Files.exists(file3)).isTrue();

        // Create a CleanUpComponentImpl with the temp directory
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectory");
        field.setAccessible(true);
        field.set(cleanUpComponent, tempDir.toString());

        // Execute clean
        cleanUpComponent.clean();

        // Verify all contents are deleted but the directory itself remains
        assertThat(Files.exists(tempDir)).isTrue();
        assertThat(Files.exists(file1)).isFalse();
        assertThat(Files.exists(file2)).isFalse();
        assertThat(Files.exists(subDir)).isFalse();
        assertThat(Files.exists(file3)).isFalse();
    }

    @Test
    void cleanShouldHandleNonExistentDirectory() throws Exception {
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectory");
        field.setAccessible(true);
        field.set(cleanUpComponent, "/tmp/non-existent-directory-12345");

        // Should not throw exception
        cleanUpComponent.clean();
    }

    @Test
    void cleanShouldHandleEmptyDirectory(@TempDir Path tempDir) throws Exception {
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectory");
        field.setAccessible(true);
        field.set(cleanUpComponent, tempDir.toString());

        // Should not throw exception on empty directory
        cleanUpComponent.clean();
        
        assertThat(Files.exists(tempDir)).isTrue();
    }
}

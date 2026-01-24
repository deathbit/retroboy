package com.github.deathbit.retroboy.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "cleanup.directories[0]=${java.io.tmpdir}/test-cleanup"
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
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectories");
        field.setAccessible(true);
        field.set(cleanUpComponent, Collections.singletonList(tempDir.toString()));

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
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectories");
        field.setAccessible(true);
        field.set(cleanUpComponent, Collections.singletonList("/tmp/non-existent-directory-12345"));

        // Should not throw exception
        cleanUpComponent.clean();
    }

    @Test
    void cleanShouldHandleEmptyDirectory(@TempDir Path tempDir) throws Exception {
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectories");
        field.setAccessible(true);
        field.set(cleanUpComponent, Collections.singletonList(tempDir.toString()));

        // Should not throw exception on empty directory
        cleanUpComponent.clean();
        
        assertThat(Files.exists(tempDir)).isTrue();
    }

    @Test
    void cleanShouldDeleteAllFilesInMultipleDirectories(@TempDir Path tempDir1, @TempDir Path tempDir2) throws Exception {
        // Create test files in first directory
        Path file1 = tempDir1.resolve("file1.txt");
        Path subDir1 = tempDir1.resolve("subdir1");
        Files.createFile(file1);
        Files.createDirectory(subDir1);

        // Create test files in second directory
        Path file2 = tempDir2.resolve("file2.txt");
        Path subDir2 = tempDir2.resolve("subdir2");
        Files.createFile(file2);
        Files.createDirectory(subDir2);

        // Verify files exist
        assertThat(Files.exists(file1)).isTrue();
        assertThat(Files.exists(subDir1)).isTrue();
        assertThat(Files.exists(file2)).isTrue();
        assertThat(Files.exists(subDir2)).isTrue();

        // Create a CleanUpComponentImpl with multiple directories
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectories");
        field.setAccessible(true);
        field.set(cleanUpComponent, Arrays.asList(tempDir1.toString(), tempDir2.toString()));

        // Execute clean
        cleanUpComponent.clean();

        // Verify all contents are deleted from both directories
        assertThat(Files.exists(tempDir1)).isTrue();
        assertThat(Files.exists(file1)).isFalse();
        assertThat(Files.exists(subDir1)).isFalse();
        
        assertThat(Files.exists(tempDir2)).isTrue();
        assertThat(Files.exists(file2)).isFalse();
        assertThat(Files.exists(subDir2)).isFalse();
    }

    @Test
    void cleanShouldHandleEmptyListOfDirectories() throws Exception {
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectories");
        field.setAccessible(true);
        field.set(cleanUpComponent, Collections.emptyList());

        // Should not throw exception
        cleanUpComponent.clean();
    }

    @Test
    void cleanShouldHandleNullListOfDirectories() throws Exception {
        CleanUpComponentImpl cleanUpComponent = new CleanUpComponentImpl();
        java.lang.reflect.Field field = CleanUpComponentImpl.class.getDeclaredField("cleanupDirectories");
        field.setAccessible(true);
        field.set(cleanUpComponent, null);

        // Should not throw exception
        cleanUpComponent.clean();
    }
}

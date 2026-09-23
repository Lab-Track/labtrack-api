package com.labtrack.labtrack.service;

import com.labtrack.labtrack.exception.InvalidPhotoFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhotoStorageServiceTest {

    @TempDir
    Path tempDir;

    private PhotoStorageService photoStorageService;

    @BeforeEach
    void setUp() {
        photoStorageService = new PhotoStorageService(
                tempDir.resolve("equipment-photos").toString(),
                "/uploads/equipment-photos",
                "http://localhost:8080");
    }

    @Test
    void shouldStoreJpgFile_AndReturnAbsolutePublicUrlEndingInJpg() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "multimetro.jpg", "image/jpeg", "conteudo-fake".getBytes());

        String url = photoStorageService.store(file);

        assertThat(url).startsWith("http://localhost:8080/uploads/equipment-photos/");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    void shouldStorePngFile_AndWriteItToDisk() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "sensor.png", "image/png", "conteudo-fake".getBytes());

        String url = photoStorageService.store(file);

        String filename = url.substring(url.lastIndexOf('/') + 1);
        Path stored = tempDir.resolve("equipment-photos").resolve(filename);
        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.readAllBytes(stored)).isEqualTo("conteudo-fake".getBytes());
    }

    @Test
    void shouldRejectFile_WhenContentTypeIsNotJpgOrPng() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "documento.pdf", "application/pdf", "conteudo-fake".getBytes());

        assertThatThrownBy(() -> photoStorageService.store(file))
                .isInstanceOf(InvalidPhotoFileException.class)
                .hasMessageContaining("JPG");
    }

    @Test
    void shouldRejectFile_WhenLargerThan5Mb() {
        byte[] oversized = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "foto", "grande.jpg", "image/jpeg", oversized);

        assertThatThrownBy(() -> photoStorageService.store(file))
                .isInstanceOf(InvalidPhotoFileException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "vazio.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> photoStorageService.store(file))
                .isInstanceOf(InvalidPhotoFileException.class);
    }

    @Test
    void shouldGenerateDifferentFilenames_ForTwoUploadsWithSameOriginalName() {
        MockMultipartFile fileA = new MockMultipartFile(
                "foto", "mesmo-nome.jpg", "image/jpeg", "a".getBytes());
        MockMultipartFile fileB = new MockMultipartFile(
                "foto", "mesmo-nome.jpg", "image/jpeg", "b".getBytes());

        String urlA = photoStorageService.store(fileA);
        String urlB = photoStorageService.store(fileB);

        assertThat(urlA).isNotEqualTo(urlB);
    }
}

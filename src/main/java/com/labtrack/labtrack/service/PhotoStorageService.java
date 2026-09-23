package com.labtrack.labtrack.service;

import com.labtrack.labtrack.exception.InvalidPhotoFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PhotoStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png");

    private final Path uploadDir;
    private final String publicPathPrefix;
    private final String baseUrl;

    public PhotoStorageService(
            @Value("${app.upload-dir}") String uploadDir,
            @Value("${app.upload-url-prefix}") String publicPathPrefix,
            @Value("${app.base-url}") String baseUrl) {
        this.uploadDir = Path.of(uploadDir);
        this.publicPathPrefix = publicPathPrefix;
        this.baseUrl = baseUrl;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPhotoFileException("Arquivo de foto não enviado");
        }

        String extension = ALLOWED_CONTENT_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidPhotoFileException("Formato não aceito; envie um arquivo JPG ou PNG");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidPhotoFileException("Arquivo maior que o limite de 5MB");
        }

        String filename = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar a foto", e);
        }

        log.info("Foto salva: {}", filename);
        return baseUrl + publicPathPrefix + "/" + filename;
    }
}

package com.etour.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.etour.exception.IllegalOperationException;

/**
 * Local-disk file storage for uploads (tour media, Excel batches). Swap the
 * implementation for an S3/Blob client later without touching callers -
 * that's the point of hiding it behind this one class.
 */
@Component
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalOperationException("Uploaded file is empty");
        }
        try {
            Path targetDir = Paths.get(uploadDir, subfolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            String original = Path.of(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                    .getFileName().toString();
            String storedName = UUID.randomUUID() + "_" + original;

            Path target = targetDir.resolve(storedName);
            file.transferTo(target);

            return "/" + uploadDir + "/" + subfolder + "/" + storedName;
        } catch (IOException e) {
            throw new IllegalOperationException("Failed to store file: " + e.getMessage());
        }
    }
}

package com.cashly.cashly_api.shared.storage.infrastructure.adapters;

import com.cashly.cashly_api.shared.storage.domain.exceptions.FileNotFoundException;
import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import com.cashly.cashly_api.shared.storage.domain.exceptions.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalFileStorageService(String storageLocation) {
        this.rootLocation = Paths.get(storageLocation);
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location",
                    e);
        }
    }

    @Override
    public String store(String originalFilename, InputStream inputStream,
            String contentType) {
        try {
            if (inputStream.available() == 0) {
                throw new StorageException(
                        "Failed to store empty file: " + originalFilename);
            }

            String extension = getFileExtension(originalFilename);
            String newFilename = buildStoragePath(originalFilename, extension);

            Path destinationFile = rootLocation.resolve(Paths.get(newFilename))
                    .normalize().toAbsolutePath();

            if (!isWithinRootLocation(destinationFile)) {
                throw new StorageException(
                        "Cannot store file outside storage directory");
            }

            Files.createDirectories(destinationFile.getParent());
            Files.copy(inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING);

            return newFilename;
        } catch (IOException e) {
            throw new StorageException(
                    "Failed to store file: " + originalFilename, e);
        }
    }

    private String buildStoragePath(String originalFilename, String extension) {
        String directory = extractDirectory(originalFilename);
        String uniqueFilename = UUID.randomUUID() + extension;

        if (directory.isEmpty()) {
            return uniqueFilename;
        }
        return directory + uniqueFilename;
    }

    private String extractDirectory(String filename) {
        if (filename == null || !filename.contains("/")) {
            return "";
        }
        return filename.substring(0, filename.lastIndexOf("/") + 1);
    }

    private boolean isWithinRootLocation(Path path) {
        return path.toAbsolutePath().startsWith(rootLocation.toAbsolutePath());
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize()
                    .toAbsolutePath();

            if (!isWithinRootLocation(file)) {
                throw new StorageException(
                        "Cannot delete file outside storage directory");
            }

            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public byte[] load(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize()
                    .toAbsolutePath();

            if (!isWithinRootLocation(file)) {
                throw new StorageException(
                        "Cannot read file outside storage directory");
            }

            if (!Files.exists(file)) {
                throw new FileNotFoundException(filename);
            }

            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new StorageException("Failed to read file: " + filename, e);
        }
    }

    @Override
    public boolean exists(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
        return isWithinRootLocation(file) && Files.exists(file);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}

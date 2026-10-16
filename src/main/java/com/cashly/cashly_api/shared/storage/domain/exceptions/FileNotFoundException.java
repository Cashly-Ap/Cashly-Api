package com.cashly.cashly_api.shared.storage.domain.exceptions;

public class FileNotFoundException extends StorageException {

    public FileNotFoundException(String filePath) {
        super("File not found: " + filePath);
    }
}

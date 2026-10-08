package com.cashly.cashly_api.products.infrastructure.integration;

import com.cashly.cashly_api.products.application.contracts.ProductFileStorageGateway;
import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class LocalProductFileStorageGatewayAdapter implements ProductFileStorageGateway {
    private final FileStorageService storage;

    public LocalProductFileStorageGatewayAdapter(FileStorageService storage) {
        this.storage = storage;
    }

    @Override
    public String store(String filename, InputStream inputStream, String contentType) {
        return storage.store(filename, inputStream, contentType);
    }

    @Override
    public void remove(String filePath) {
        storage.delete(filePath);
    }
}

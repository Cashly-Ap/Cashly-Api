package com.cashly.cashly_api.catalog.products.reporting.infrastructure.integration;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.MediaGateway;
import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
public class FileStorageMediaAdapter implements MediaGateway {
    private final FileStorageService storage;
    public FileStorageMediaAdapter(FileStorageService storage) { this.storage = storage; }
    public byte[] load(String path) throws IOException { return storage.load(path); }
}
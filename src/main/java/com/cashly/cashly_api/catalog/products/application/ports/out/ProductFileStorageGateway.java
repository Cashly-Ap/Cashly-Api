package com.cashly.cashly_api.catalog.products.application.ports.out;

import java.io.InputStream;

public interface ProductFileStorageGateway {
    String store(String filename, InputStream inputStream, String contentType);

    void remove(String filePath);
}

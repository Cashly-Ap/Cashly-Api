package com.cashly.cashly_api.products.application.contracts;

import java.io.InputStream;

public interface ProductFileStorageGateway {
    String store(String filename, InputStream inputStream, String contentType);

    void remove(String filePath);
}

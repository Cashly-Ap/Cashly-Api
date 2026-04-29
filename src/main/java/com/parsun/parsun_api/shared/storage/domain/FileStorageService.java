package com.parsun.parsun_api.shared.storage.domain;

import java.io.InputStream;

public interface FileStorageService {

    String store(String filename, InputStream inputStream, String contentType);

    void delete(String filePath);

    byte[] load(String filePath);

    boolean exists(String filePath);
}

package com.cashly.cashly_api.platform.company.infrastructure;

import com.cashly.cashly_api.platform.company.application.CompanyMediaStorage;
import com.cashly.cashly_api.shared.storage.infrastructure.images.SafeImageStorage;
import org.springframework.stereotype.Component;

@Component
public class CompanyMediaStorageAdapter implements CompanyMediaStorage {
    private final SafeImageStorage images;

    public CompanyMediaStorageAdapter(SafeImageStorage images) {
        this.images = images;
    }

    @Override
    public String store(byte[] bytes) {
        return images.store(bytes, "company");
    }

    @Override
    public byte[] load(String path) {
        return images.load(path);
    }

    @Override
    public void deleteAfterCommit(String path) {
        images.deleteAfterCommit(path);
    }
}

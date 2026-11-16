package com.cashly.cashly_api.platform.company.application.ports.out;

public interface CompanyMediaStorage {
    String store(byte[] bytes);
    byte[] load(String path);
    void deleteAfterCommit(String path);
}

package com.cashly.cashly_api.shared.storage.infrastructure;

import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.type:local}")
    private String storageType;

    @Value("${storage.local.path:${storage.location:./uploads}}")
    private String localPath;

    @Value("${storage.s3.endpoint:#{null}}")
    private String s3Endpoint;

    @Value("${storage.s3.access-key:#{null}}")
    private String s3AccessKey;

    @Value("${storage.s3.secret-key:#{null}}")
    private String s3SecretKey;

    @Value("${storage.s3.bucket:cashly-files}")
    private String s3Bucket;

    @Value("${storage.s3.region:us-east-1}")
    private String s3Region;

    @Bean
    public FileStorageService fileStorageService() {
        if ("s3".equalsIgnoreCase(storageType)) {
            return new S3FileStorageService(s3Endpoint, s3AccessKey, s3SecretKey, s3Bucket, s3Region);
        }
        return new LocalFileStorageService(localPath);
    }
}

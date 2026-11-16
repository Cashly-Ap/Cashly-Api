# Storage Implementation Guide

## Current Implementation

The application uses **S3-compatible object storage (MinIO)** by default, configured with `storage.type=s3`. Files are stored in the `lexand-files` bucket.

**Active storage**: MinIO is running in production. The `S3FileStorageService` is the default implementation.

### Known Folders in Use
| Folder | Used By | Description |
|--------|---------|-------------|
| `payment-proofs/` | RECEIVABLES module | Payment proof files uploaded by merchants |

## MinIO Setup (Local Development)

MinIO is included in `docker-compose.yaml` and provides S3-compatible object storage.

### Access MinIO Console
- URL: http://localhost:9001
- User: `minioadmin`
- Password: `minioadmin`

### Bucket
The bucket `lexand-files` is used in all environments. It is created automatically on first use or must be created manually in MinIO Console if using a fresh instance:
1. Go to MinIO Console
2. Click "Create Bucket"
3. Name it `lexand-files`

## S3/MinIO Implementation (Active)

The S3-compatible storage is already implemented and in use. The `S3FileStorageService` class handles all file operations.

### Dependencies in `pom.xml`

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.25.0</version>
</dependency>
```

### S3FileStorageService

```java
package com.lexand.lexand_api.shared.storage.infrastructure;

import com.lexand.lexand_api.shared.storage.domain.FileStorageService;
import com.lexand.lexand_api.shared.storage.domain.StorageException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3FileStorageService(String endpoint, String accessKey, String secretKey, String bucketName) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(() -> new AwsCredentials() {
                @Override public String accessKeyId() { return accessKey; }
                @Override public String secretAccessKey() { return secretKey; }
            })
            .forcePathStyle(true)  // Required for MinIO
            .build();
    }

    @Override
    public String store(String filename, InputStream inputStream, String contentType) {
        try {
            String extension = getFileExtension(filename);
            String directory = extractDirectory(filename);
            String key = directory + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
            return key;
        } catch (Exception e) {
            throw new StorageException("Failed to store file in S3: " + filename, e);
        }
    }

    @Override
    public byte[] load(String filePath) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException(filePath);
        } catch (Exception e) {
            throw new StorageException("Failed to load file from S3: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new StorageException("Failed to delete file from S3: " + filePath, e);
        }
    }

    @Override
    public boolean exists(String filePath) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractDirectory(String filename) {
        if (filename == null || !filename.contains("/")) return "";
        return filename.substring(0, filename.lastIndexOf("/") + 1);
    }
}
```

### 3. Storage Configuration

```java
package com.lexand.lexand_api.shared.storage.infrastructure.config;

import com.lexand.lexand_api.shared.storage.domain.FileStorageService;
import com.lexand.lexand_api.shared.storage.infrastructure.LocalFileStorageService;
import com.lexand.lexand_api.shared.storage.infrastructure.S3FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.type:local}")
    private String storageType;

    @Value("${storage.location:uploads}")
    private String localStorageLocation;

    @Value("${storage.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${storage.s3.access-key:}")
    private String s3AccessKey;

    @Value("${storage.s3.secret-key:}")
    private String s3SecretKey;

    @Value("${storage.s3.bucket:}")
    private String s3Bucket;

    @Bean
    public FileStorageService fileStorageService() {
        if ("s3".equalsIgnoreCase(storageType)) {
            return new S3FileStorageService(s3Endpoint, s3AccessKey, s3SecretKey, s3Bucket);
        }
        return new LocalFileStorageService(localStorageLocation);
    }
}
```

### 4. Configuration in `application.properties`

```properties
# Storage Configuration
# Options: local, s3
storage.type=s3
storage.location=/app/uploads

# S3/MinIO Configuration (active when storage.type=s3)
storage.s3.endpoint=http://minio:9000
storage.s3.access-key=minioadmin
storage.s3.secret-key=minioadmin
storage.s3.bucket=lexand-files
```

### 5. For Production (S3-compatible storage)

Change the endpoint and credentials to match your S3-compatible provider:

```properties
storage.type=s3
storage.s3.endpoint=${STORAGE_S3_ENDPOINT}
storage.s3.access-key=${STORAGE_S3_ACCESS_KEY}
storage.s3.secret-key=${STORAGE_S3_SECRET_KEY}
storage.s3.bucket=${STORAGE_S3_BUCKET}
```

## Environment Variables for Docker

Already configured in `docker-compose.yaml` app service:

```yaml
environment:
  - STORAGE_TYPE=${STORAGE_TYPE:-s3}
  - STORAGE_S3_ENDPOINT=${STORAGE_S3_ENDPOINT:-http://minio:9000}
  - STORAGE_S3_ACCESS_KEY=${STORAGE_S3_ACCESS_KEY:-minioadmin}
  - STORAGE_S3_SECRET_KEY=${STORAGE_S3_SECRET_KEY:-minioadmin}
  - STORAGE_S3_BUCKET=${STORAGE_S3_BUCKET:-lexand-files}
```

## Fallback: Local Storage

If you need to run without MinIO (e.g., for quick local tests without Docker), set `storage.type=local`. Files will be stored at `/app/uploads/`.

**Note**: Local storage is not recommended for production — files are lost if the container is recreated without a volume backup and do not scale horizontally.

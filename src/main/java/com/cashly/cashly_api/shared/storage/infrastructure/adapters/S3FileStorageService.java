package com.cashly.cashly_api.shared.storage.infrastructure.adapters;

import com.cashly.cashly_api.shared.storage.domain.exceptions.FileNotFoundException;
import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import com.cashly.cashly_api.shared.storage.domain.exceptions.StorageException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3FileStorageService(String endpoint, String accessKey, String secretKey, String bucketName, String region) {
        this.bucketName = bucketName;

        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));

        if (endpoint != null && accessKey != null) {
            builder.endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .serviceConfiguration(s -> s.pathStyleAccessEnabled(true));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        this.s3Client = builder.build();
    }

    @Override
    public String store(String filename, InputStream inputStream, String contentType) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                throw new StorageException("Failed to store empty file: " + filename);
            }

            String extension = getFileExtension(filename);
            String directory = extractDirectory(filename);
            String key = directory + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            return key;
        } catch (IOException e) {
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
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
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
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractDirectory(String filename) {
        if (filename == null || !filename.contains("/")) {
            return "";
        }
        return filename.substring(0, filename.lastIndexOf("/") + 1);
    }
}

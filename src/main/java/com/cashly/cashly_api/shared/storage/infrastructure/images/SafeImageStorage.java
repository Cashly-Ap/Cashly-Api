package com.cashly.cashly_api.shared.storage.infrastructure.images;

import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class SafeImageStorage {
    private final FileStorageService storage;
    public SafeImageStorage(FileStorageService storage) { this.storage = storage; }

    public String store(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty() || file.getSize() > 5L * 1024 * 1024)
            throw new IllegalArgumentException("Upload a PNG or JPEG image up to 5 MB");
        try {
            return store(file.getBytes(), prefix);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot read image", exception);
        }
    }

    public String store(byte[] bytes, String prefix) {
        if (bytes == null || bytes.length == 0 || bytes.length > 5L * 1024 * 1024)
            throw new IllegalArgumentException("Upload a PNG or JPEG image up to 5 MB");
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Invalid PNG or JPEG image");
            var reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg") && !format.equalsIgnoreCase("jpg"))
                    throw new IllegalArgumentException("Only PNG and JPEG images are supported");
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 4096 || height > 4096)
                    throw new IllegalArgumentException("Image dimensions must be between 1 and 4096 pixels");
                var image = reader.read(0);
                var output = new ByteArrayOutputStream();
                if (!ImageIO.write(image, "png", output)) throw new IllegalArgumentException("Cannot decode image");
                String path = storage.store(prefix + "/image.png", new ByteArrayInputStream(output.toByteArray()), "image/png");
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override public void afterCompletion(int status) { if (status != STATUS_COMMITTED) deleteQuietly(path); }
                    });
                }
                return path;
            } finally { reader.dispose(); }
        } catch (IOException exception) { throw new IllegalArgumentException("Invalid PNG or JPEG image", exception); }
    }
    public byte[] load(String path) { return storage.load(path); }
    public void deleteAfterCommit(String path) {
        if (path == null || path.isBlank()) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { deleteQuietly(path); }
            });
        } else deleteQuietly(path);
    }
    private void deleteQuietly(String path) {
        try { storage.delete(path); } catch (RuntimeException ignored) { /* Orphan cleanup must not invalidate a committed profile. */ }
    }
}

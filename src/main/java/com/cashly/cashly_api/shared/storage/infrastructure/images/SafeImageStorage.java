package com.cashly.cashly_api.shared.storage.infrastructure.images;

import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class SafeImageStorage {
    private static final int MAX_IMAGE_SIDE = 2048;
    private final FileStorageService storage;
    public SafeImageStorage(FileStorageService storage) {
        this.storage = storage;
    }

    public String store(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Upload a PNG or JPEG image");
        try {
            return store(file.getBytes(), prefix);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot read image", exception);
        }
    }

    public String store(byte[] bytes, String prefix) {
        byte[] normalized = normalize(bytes);
        String path = storage.store(prefix + "/image.png",
                new ByteArrayInputStream(normalized), "image/png");
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager
                    .registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status != STATUS_COMMITTED)
                                deleteQuietly(path);
                        }
                    });
        }
        return path;
    }

    /** Validates and normalizes image bytes without writing to storage. */
    public static byte[] normalize(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            throw new IllegalArgumentException("Upload a PNG or JPEG image");
        try (var input = ImageIO
                .createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext())
                throw new IllegalArgumentException("Invalid PNG or JPEG image");
            var reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png")
                        && !format.equalsIgnoreCase("jpeg")
                        && !format.equalsIgnoreCase("jpg"))
                    throw new IllegalArgumentException(
                            "Only PNG and JPEG images are supported");
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1)
                    throw new IllegalArgumentException(
                            "Invalid image dimensions");
                var parameters = reader.getDefaultReadParam();
                int sampling = Math.max(1,
                        Math.max(width, height) / MAX_IMAGE_SIDE);
                parameters.setSourceSubsampling(sampling, sampling, 0, 0);
                var image = fit(reader.read(0, parameters));
                var output = new ByteArrayOutputStream();
                if (!ImageIO.write(image, "png", output))
                    throw new IllegalArgumentException("Cannot decode image");
                return output.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid PNG or JPEG image",
                    exception);
        }
    }
    private static BufferedImage fit(BufferedImage source) {
        int longest = Math.max(source.getWidth(), source.getHeight());
        if (longest <= MAX_IMAGE_SIDE)
            return source;
        double scale = (double) MAX_IMAGE_SIDE / longest;
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        var resized = new BufferedImage(width, height,
                source.getColorModel().hasAlpha()
                        ? BufferedImage.TYPE_INT_ARGB
                        : BufferedImage.TYPE_INT_RGB);
        var graphics = resized.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    public byte[] load(String path) {
        return storage.load(path);
    }
    public void deleteAfterCommit(String path) {
        if (path == null || path.isBlank())
            return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager
                    .registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            deleteQuietly(path);
                        }
                    });
        } else
            deleteQuietly(path);
    }
    private void deleteQuietly(String path) {
        try {
            storage.delete(path);
        } catch (RuntimeException ignored) {
            /* Orphan cleanup must not invalidate a committed profile. */ }
    }
}

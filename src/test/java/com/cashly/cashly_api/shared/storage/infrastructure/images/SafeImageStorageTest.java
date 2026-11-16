package com.cashly.cashly_api.shared.storage.infrastructure.images;
import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class SafeImageStorageTest {
    private final FileStorageService files = mock(FileStorageService.class);
    private final SafeImageStorage images = new SafeImageStorage(files);
    @Test
    void rejectsSvgAndMalformedUploads() {
        assertThatThrownBy(() -> images.store(new MockMultipartFile("file",
                "a.svg", "image/svg+xml", "<svg/>".getBytes()), "company"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> images.store(
                new MockMultipartFile("file", new byte[5 * 1024 * 1024 + 1]),
                "company")).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(files);
    }
    @Test
    void decodesRasterRatherThanTrustingFilenameAndStoresSanitizedPng()
            throws Exception {
        var source = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB),
                "jpeg", source);
        when(files.store(eq("company/image.png"), any(), eq("image/png")))
                .thenReturn("company/safe.png");
        assertThat(images.store(
                new MockMultipartFile("file", "misleading.exe",
                        "application/octet-stream", source.toByteArray()),
                "company")).isEqualTo("company/safe.png");
        var stream = org.mockito.ArgumentCaptor.forClass(InputStream.class);
        verify(files).store(eq("company/image.png"), stream.capture(),
                eq("image/png"));
        var decoded = ImageIO.read(stream.getValue());
        assertThat(decoded.getWidth()).isEqualTo(4);
        assertThat(decoded.getHeight()).isEqualTo(3);
    }
    @Test
    void downsamplesLargeDimensionsBeforeStoringAndKeepsAlphaAndAspectRatio()
            throws Exception {
        var original = new BufferedImage(6000, 3000,
                BufferedImage.TYPE_INT_ARGB);
        var graphics = original.createGraphics();
        graphics.setColor(new java.awt.Color(255, 0, 0, 128));
        graphics.fillRect(0, 0, 6000, 3000);
        graphics.dispose();
        var source = new ByteArrayOutputStream();
        ImageIO.write(original, "png", source);
        when(files.store(eq("company/image.png"), any(), eq("image/png")))
                .thenReturn("company/safe.png");
        images.store(new MockMultipartFile("file", source.toByteArray()),
                "company");
        var stream = org.mockito.ArgumentCaptor.forClass(InputStream.class);
        verify(files).store(eq("company/image.png"), stream.capture(),
                eq("image/png"));
        var normalized = ImageIO.read(stream.getValue());
        assertThat(normalized.getWidth()).isEqualTo(2048);
        assertThat(normalized.getHeight()).isEqualTo(1024);
        assertThat((normalized.getRGB(100, 100) >>> 24) & 255).isEqualTo(128);
    }
    @Test
    void acceptsOriginalLargerThanFiveMegabytesAndPersistsReducedBytes()
            throws Exception {
        var original = new BufferedImage(2600, 900, BufferedImage.TYPE_INT_RGB);
        var random = new java.util.Random(42);
        for (int y = 0; y < 900; y++)
            for (int x = 0; x < 2600; x++)
                original.setRGB(x, y, random.nextInt(0x1000000));
        var source = new ByteArrayOutputStream();
        ImageIO.write(original, "png", source);
        assertThat(source.size()).isGreaterThan(5 * 1024 * 1024);
        when(files.store(eq("company/image.png"), any(), eq("image/png")))
                .thenReturn("company/reduced.png");
        images.store(new MockMultipartFile("file", source.toByteArray()),
                "company");
        var stream = org.mockito.ArgumentCaptor.forClass(InputStream.class);
        verify(files).store(eq("company/image.png"), stream.capture(),
                eq("image/png"));
        byte[] persisted = stream.getValue().readAllBytes();
        assertThat(persisted.length).isLessThan(source.size());
        var decoded = ImageIO.read(new ByteArrayInputStream(persisted));
        assertThat(decoded.getWidth()).isEqualTo(2048);
        assertThat(decoded.getHeight()).isEqualTo(709);
    }
    @Test
    void supportsVeryLongImagesAndDoesNotUpscaleSmallOnes() throws Exception {
        for (int[] dimensions : new int[][]{{20000, 1}, {1, 20000}, {20, 30}}) {
            var original = new BufferedImage(dimensions[0], dimensions[1],
                    BufferedImage.TYPE_INT_ARGB);
            var source = new ByteArrayOutputStream();
            ImageIO.write(original, "png", source);
            var reduced = ImageIO.read(new ByteArrayInputStream(
                    SafeImageStorage.normalize(source.toByteArray())));
            assertThat(Math.max(reduced.getWidth(), reduced.getHeight()))
                    .isLessThanOrEqualTo(2048);
            if (dimensions[0] == 20) {
                assertThat(reduced.getWidth()).isEqualTo(20);
                assertThat(reduced.getHeight()).isEqualTo(30);
            }
        }
    }
}

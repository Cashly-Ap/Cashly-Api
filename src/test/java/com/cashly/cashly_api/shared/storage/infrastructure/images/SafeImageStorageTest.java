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
    private final FileStorageService files=mock(FileStorageService.class);
    private final SafeImageStorage images=new SafeImageStorage(files);
    @Test void rejectsSvgAndOversizedUploads() {
        assertThatThrownBy(() -> images.store(new MockMultipartFile("file","a.svg","image/svg+xml","<svg/>".getBytes()),"company"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> images.store(new MockMultipartFile("file",new byte[5*1024*1024+1]),"company"))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(files);
    }
    @Test void decodesRasterRatherThanTrustingFilenameAndStoresSanitizedPng() throws Exception {
        var source=new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4,3,BufferedImage.TYPE_INT_RGB),"jpeg",source);
        when(files.store(eq("company/image.png"),any(),eq("image/png"))).thenReturn("company/safe.png");
        assertThat(images.store(new MockMultipartFile("file","misleading.exe","application/octet-stream",source.toByteArray()),"company"))
            .isEqualTo("company/safe.png");
        var stream = org.mockito.ArgumentCaptor.forClass(InputStream.class);
        verify(files).store(eq("company/image.png"),stream.capture(),eq("image/png"));
        var decoded=ImageIO.read(stream.getValue());
        assertThat(decoded.getWidth()).isEqualTo(4);
        assertThat(decoded.getHeight()).isEqualTo(3);
    }
    @Test void rejectsDimensionsBeforeStoring() throws Exception {
        var source=new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4097,1,BufferedImage.TYPE_INT_RGB),"png",source);
        assertThatThrownBy(() -> images.store(new MockMultipartFile("file",source.toByteArray()),"company"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("dimensions");
        verifyNoInteractions(files);
    }
}

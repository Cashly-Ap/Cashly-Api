package com.cashly.cashly_api.products.infrastructure.integration;

import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import com.cashly.cashly_api.shared.storage.domain.StorageException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LocalProductFileStorageGatewayAdapterTest {

    @Test
    void shouldDelegateUploadToSharedStorage() {
        FileStorageService storage = mock(FileStorageService.class);
        var adapter = new LocalProductFileStorageGatewayAdapter(storage);
        var input = new ByteArrayInputStream(new byte[]{1, 2});
        when(storage.store("products/photo.jpg", input, "image/jpeg")).thenReturn("stored/photo.jpg");

        String path = adapter.store("products/photo.jpg", input, "image/jpeg");

        assertThat(path).isEqualTo("stored/photo.jpg");
        verify(storage).store("products/photo.jpg", input, "image/jpeg");
    }

    @Test
    void shouldDelegateRemovalToSharedStorage() {
        FileStorageService storage = mock(FileStorageService.class);
        var adapter = new LocalProductFileStorageGatewayAdapter(storage);

        adapter.remove("stored/photo.jpg");

        verify(storage).delete("stored/photo.jpg");
    }

    @Test
    void shouldPropagateStorageFailuresWithoutChangingThem() {
        FileStorageService storage = mock(FileStorageService.class);
        var adapter = new LocalProductFileStorageGatewayAdapter(storage);
        var failure = new StorageException("Storage unavailable");
        when(storage.store(anyString(), any(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> adapter.store("products/photo.jpg", new ByteArrayInputStream(new byte[0]),
                "image/jpeg")).isSameAs(failure);
    }

    @Test
    void shouldPropagateRemovalFailuresWithoutChangingThem() {
        FileStorageService storage = mock(FileStorageService.class);
        var adapter = new LocalProductFileStorageGatewayAdapter(storage);
        var failure = new StorageException("Cannot remove file");
        doThrow(failure).when(storage).delete("stored/photo.jpg");

        assertThatThrownBy(() -> adapter.remove("stored/photo.jpg")).isSameAs(failure);
    }
}

package com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class FileStorageMediaAdapterTest {
    @Test void loadReturnsExactBytesAndStoragePath() throws Exception {
        var storage = mock(FileStorageService.class);
        var bytes = new byte[]{1, 2, 3};
        when(storage.load("photo.png")).thenReturn(bytes);
        assertSame(bytes, new FileStorageMediaAdapter(storage).load("photo.png"));
        verify(storage).load("photo.png");
    }
    @Test void actualIOExceptionPreservesFailureIdentity() {
        var storage = mock(FileStorageService.class);
        var failure = new IOException("Storage unavailable");
        when(storage.load("photo.png")).thenAnswer(invocation -> { throw failure; });
        assertSame(failure, assertThrows(IOException.class, () -> new FileStorageMediaAdapter(storage).load("photo.png")));
    }
    @Test void runtimeStorageFailurePreservesFailureIdentity() {
        var storage = mock(FileStorageService.class);
        var failure = new IllegalStateException("Storage unavailable");
        when(storage.load("photo.png")).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> new FileStorageMediaAdapter(storage).load("photo.png")));
    }
}

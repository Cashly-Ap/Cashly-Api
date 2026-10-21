package com.cashly.cashly_api.shared.storage.infrastructure.web;

import com.cashly.cashly_api.shared.storage.domain.services.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileControllerIdentityMediaTest {
    @Test
    void catalogFileAccessCannotBypassUserOrCompanyMediaEndpoints() {
        var files = mock(FileStorageService.class);
        var controller = new FileController(files);
        for (String path : new String[]{"users/user-id/photo.png", "company/logo.png", "products/../users/user-id/photo.png"}) {
            var request = new MockHttpServletRequest("GET", "/api/files/" + path);
            assertThat(controller.serveFile(request).getStatusCode().value()).isEqualTo(404);
        }
        verifyNoInteractions(files);
    }

    @Test
    void catalogImagesContinueUsingTheExistingStorageRoute() {
        var files = mock(FileStorageService.class);
        when(files.exists("products/product.png")).thenReturn(true);
        when(files.load("products/product.png")).thenReturn(new byte[]{1});
        var request = new MockHttpServletRequest("GET", "/api/files/products/product.png");
        assertThat(new FileController(files).serveFile(request).getStatusCode().value()).isEqualTo(200);
    }
}

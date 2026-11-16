package com.cashly.cashly_api.infrastructure.config;

import com.cashly.cashly_api.platform.auth.authorization.domain.exceptions.InvalidPermissionDependencyException;
import com.cashly.cashly_api.platform.auth.authorization.domain.exceptions.UnknownPermissionCodeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPermissionErrorHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dependencyErrorReturnsTheContractShapeWithMissingRoots() {
        var exception = new InvalidPermissionDependencyException(
                "SALES_QUOTES_CREATE requiere SALES_QUOTES_VIEW_LIST",
                List.of("SALES_QUOTES_VIEW_LIST"));

        var response = handler.handleInvalidPermissionDependency(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("code", "INVALID_PERMISSION_DEPENDENCY")
                .containsEntry("message",
                        "SALES_QUOTES_CREATE requiere SALES_QUOTES_VIEW_LIST")
                .containsEntry("missing", List.of("SALES_QUOTES_VIEW_LIST"));
    }

    @Test
    void unknownPermissionErrorIsRejectedAsBadRequest() {
        var exception = new UnknownPermissionCodeException("SALES_QUOTES_TELEPORT");

        var response = handler.handleUnknownPermissionCode(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("code", "INVALID_PERMISSION_CODE")
                .containsEntry("message",
                        "Unknown permission code: SALES_QUOTES_TELEPORT");
    }
}

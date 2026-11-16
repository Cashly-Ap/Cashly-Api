package com.cashly.cashly_api.platform.auth.user.verification.infrastructure.web;

import com.cashly.cashly_api.platform.auth.user.verification.application.services.EmailVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth/email-verification")
public class EmailVerificationController {

    private final EmailVerificationService service;

    public EmailVerificationController(EmailVerificationService service) {
        this.service = service;
    }

    public record ConfirmRequest(
            @NotBlank(message = "Token is required") @Size(max = 512) String token) {
    }

    public record ConfirmResponse(boolean emailVerified) {
    }

    @PostMapping("/resend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> resend(Authentication authentication) {
        service.resend(UUID.fromString(authentication.getName()));
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore())
                .build();
    }

    @PostMapping("/confirm")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ConfirmResponse> confirm(
            @Valid @RequestBody ConfirmRequest request) {
        service.confirm(request.token());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new ConfirmResponse(true));
    }
}

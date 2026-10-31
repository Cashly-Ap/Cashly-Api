package com.cashly.cashly_api.platform.auth.mfa.infrastructure.web;

import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.platform.auth.mfa.application.services.MfaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mfa")
public class MfaController {
    private final MfaService mfa;

    public MfaController(MfaService mfa) {
        this.mfa = mfa;
    }

    public record PasswordRequest(@NotBlank @Size(max = 128) String password) {
    }
    public record CodeRequest(@NotBlank @Size(max = 64) String code) {
    }
    public record DisableRequest(@NotBlank @Size(max = 128) String password,
            @NotBlank @Size(max = 64) String code) {
    }
    public record VerifyRequest(
            @NotBlank @Size(max = 128) String challengeToken,
            @NotBlank @Size(max = 64) String code) {
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaService.Status> status(
            Authentication authentication) {
        return noStore(mfa.status(authentication.getName()));
    }

    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaService.Setup> setup(Authentication authentication,
            @Valid @RequestBody PasswordRequest request) {
        return noStore(mfa.setup(authentication.getName(), request.password()));
    }

    @PostMapping("/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaService.RecoveryCodes> enable(
            Authentication authentication,
            @Valid @RequestBody CodeRequest request) {
        return noStore(mfa.enable(authentication.getName(), request.code()));
    }

    @PostMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> disable(Authentication authentication,
            @Valid @RequestBody DisableRequest request) {
        mfa.disable(authentication.getName(), request.password(),
                request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthenticationResponse> verify(
            @Valid @RequestBody VerifyRequest request) {
        return noStore(mfa.verify(request.challengeToken(), request.code()));
    }

    private <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(body);
    }
}

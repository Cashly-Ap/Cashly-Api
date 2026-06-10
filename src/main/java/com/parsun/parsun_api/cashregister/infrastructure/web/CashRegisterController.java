package com.parsun.parsun_api.cashregister.infrastructure.web;

import com.parsun.parsun_api.cashregister.application.dto.*;
import com.parsun.parsun_api.cashregister.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.UUID;

@RestController
@RequestMapping("/api/cash-register")
public class CashRegisterController {

    private final OpenRegisterUseCase openRegisterUseCase;
    private final GetActiveRegisterUseCase getActiveRegisterUseCase;
    private final CloseRegisterUseCase closeRegisterUseCase;
    private final VerifyRegisterUseCase verifyRegisterUseCase;
    private final GetRegisterHistoryUseCase getRegisterHistoryUseCase;

    public CashRegisterController(OpenRegisterUseCase openRegisterUseCase,
                                  GetActiveRegisterUseCase getActiveRegisterUseCase,
                                  CloseRegisterUseCase closeRegisterUseCase,
                                  VerifyRegisterUseCase verifyRegisterUseCase,
                                  GetRegisterHistoryUseCase getRegisterHistoryUseCase) {
        this.openRegisterUseCase = openRegisterUseCase;
        this.getActiveRegisterUseCase = getActiveRegisterUseCase;
        this.closeRegisterUseCase = closeRegisterUseCase;
        this.verifyRegisterUseCase = verifyRegisterUseCase;
        this.getRegisterHistoryUseCase = getRegisterHistoryUseCase;
    }

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CashRegisterResponse> open(
            @Valid @RequestBody OpenRegisterRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CashRegisterResponse response = openRegisterUseCase.execute(userId, request.openedByName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CashRegisterResponse> getActive(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CashRegisterResponse response = getActiveRegisterUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CashRegisterResponse> close(
            @PathVariable UUID id,
            @Valid @RequestBody CloseRegisterRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CashRegisterResponse response = closeRegisterUseCase.execute(id, userId, request.closedByName(), request.closingNotes());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CashRegisterResponse> verify(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyRegisterRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CashRegisterResponse response = verifyRegisterUseCase.execute(id, userId, request.verifiedByName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Page<CashRegisterResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "closureDate,desc") String sort) {
        var history = getRegisterHistoryUseCase.execute(page, size, sort);
        return ResponseEntity.ok(history);
    }
}

package com.parsun.parsun_api.advances.infrastructure.web;

import com.parsun.parsun_api.advances.application.dto.request.ApplyAdvanceRequest;
import com.parsun.parsun_api.advances.application.dto.request.CreateAdvanceRequest;
import com.parsun.parsun_api.advances.application.dto.response.AdvanceListResponse;
import com.parsun.parsun_api.advances.application.dto.response.AdvanceResponse;
import com.parsun.parsun_api.advances.application.usecases.ApplyAdvanceUseCase;
import com.parsun.parsun_api.advances.application.usecases.CancelAdvanceUseCase;
import com.parsun.parsun_api.advances.application.usecases.CreateAdvanceUseCase;
import com.parsun.parsun_api.advances.application.usecases.GetAllAdvancesUseCase;
import com.parsun.parsun_api.advances.application.usecases.GetAdvanceByIdUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/advances")
public class AdvanceController {

    private final CreateAdvanceUseCase createAdvanceUseCase;
    private final GetAllAdvancesUseCase getAllAdvancesUseCase;
    private final GetAdvanceByIdUseCase getAdvanceByIdUseCase;
    private final ApplyAdvanceUseCase applyAdvanceUseCase;
    private final CancelAdvanceUseCase cancelAdvanceUseCase;

    public AdvanceController(CreateAdvanceUseCase createAdvanceUseCase,
                              GetAllAdvancesUseCase getAllAdvancesUseCase,
                              GetAdvanceByIdUseCase getAdvanceByIdUseCase,
                              ApplyAdvanceUseCase applyAdvanceUseCase,
                              CancelAdvanceUseCase cancelAdvanceUseCase) {
        this.createAdvanceUseCase = createAdvanceUseCase;
        this.getAllAdvancesUseCase = getAllAdvancesUseCase;
        this.getAdvanceByIdUseCase = getAdvanceByIdUseCase;
        this.applyAdvanceUseCase = applyAdvanceUseCase;
        this.cancelAdvanceUseCase = cancelAdvanceUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<AdvanceResponse> create(
            @Valid @RequestBody CreateAdvanceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        AdvanceResponse response = createAdvanceUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<AdvanceListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status) {
        List<AdvanceListResponse> advances = getAllAdvancesUseCase.execute(dateFrom, dateTo, customerId, status);
        return ResponseEntity.ok(advances);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<AdvanceResponse> getById(@PathVariable UUID id) {
        AdvanceResponse response = getAdvanceByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<AdvanceResponse> apply(
            @PathVariable UUID id,
            @Valid @RequestBody ApplyAdvanceRequest request) {
        AdvanceResponse response = applyAdvanceUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelAdvanceUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

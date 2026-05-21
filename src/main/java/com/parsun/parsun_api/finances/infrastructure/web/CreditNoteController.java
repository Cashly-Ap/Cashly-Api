package com.parsun.parsun_api.finances.infrastructure.web;

import com.parsun.parsun_api.finances.application.dto.request.CreateCreditNoteRequest;
import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finances/credit-notes")
public class CreditNoteController {

    private final CreateCreditNoteUseCase createCreditNoteUseCase;
    private final ApplyCreditNoteUseCase applyCreditNoteUseCase;
    private final CancelCreditNoteUseCase cancelCreditNoteUseCase;
    private final GetCreditNotesUseCase getCreditNotesUseCase;

    public CreditNoteController(CreateCreditNoteUseCase createCreditNoteUseCase,
                                ApplyCreditNoteUseCase applyCreditNoteUseCase,
                                CancelCreditNoteUseCase cancelCreditNoteUseCase,
                                GetCreditNotesUseCase getCreditNotesUseCase) {
        this.createCreditNoteUseCase = createCreditNoteUseCase;
        this.applyCreditNoteUseCase = applyCreditNoteUseCase;
        this.cancelCreditNoteUseCase = cancelCreditNoteUseCase;
        this.getCreditNotesUseCase = getCreditNotesUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CreditNoteResponse> create(
            @Valid @RequestBody CreateCreditNoteRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CreditNoteResponse response = createCreditNoteUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<CreditNoteResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status) {
        List<CreditNoteResponse> creditNotes = getCreditNotesUseCase.execute(dateFrom, dateTo, customerId, status);
        return ResponseEntity.ok(creditNotes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CreditNoteResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CreditNoteResponse> apply(@PathVariable UUID id,
                                                     @RequestParam UUID invoiceId,
                                                     @RequestParam BigDecimal amount) {
        CreditNoteResponse response = applyCreditNoteUseCase.execute(id, invoiceId, amount);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CreditNoteResponse> cancel(@PathVariable UUID id) {
        CreditNoteResponse response = cancelCreditNoteUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}

package com.parsun.parsun_api.finances.infrastructure.web;

import com.parsun.parsun_api.finances.application.dto.request.CreateDebitNoteRequest;
import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
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
@RequestMapping("/api/finances/debit-notes")
public class DebitNoteController {

    private final CreateDebitNoteUseCase createDebitNoteUseCase;
    private final ApplyDebitNoteUseCase applyDebitNoteUseCase;
    private final CancelDebitNoteUseCase cancelDebitNoteUseCase;
    private final GetDebitNotesUseCase getDebitNotesUseCase;

    public DebitNoteController(CreateDebitNoteUseCase createDebitNoteUseCase,
                               ApplyDebitNoteUseCase applyDebitNoteUseCase,
                               CancelDebitNoteUseCase cancelDebitNoteUseCase,
                               GetDebitNotesUseCase getDebitNotesUseCase) {
        this.createDebitNoteUseCase = createDebitNoteUseCase;
        this.applyDebitNoteUseCase = applyDebitNoteUseCase;
        this.cancelDebitNoteUseCase = cancelDebitNoteUseCase;
        this.getDebitNotesUseCase = getDebitNotesUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<DebitNoteResponse> create(
            @Valid @RequestBody CreateDebitNoteRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        DebitNoteResponse response = createDebitNoteUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<DebitNoteResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status) {
        List<DebitNoteResponse> debitNotes = getDebitNotesUseCase.execute(dateFrom, dateTo, customerId, status);
        return ResponseEntity.ok(debitNotes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<DebitNoteResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<DebitNoteResponse> apply(@PathVariable UUID id,
                                                    @RequestParam UUID invoiceId,
                                                    @RequestParam BigDecimal amount) {
        DebitNoteResponse response = applyDebitNoteUseCase.execute(id, invoiceId, amount);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<DebitNoteResponse> cancel(@PathVariable UUID id) {
        DebitNoteResponse response = cancelDebitNoteUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}

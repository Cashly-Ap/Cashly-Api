package com.cashly.cashly_api.debit_notes.infrastructure.web;

import com.cashly.cashly_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.usecases.CreateDebitNoteUseCase;
import com.cashly.cashly_api.debit_notes.application.usecases.DeleteDebitNoteUseCase;
import com.cashly.cashly_api.debit_notes.application.usecases.GetDebitNoteByIdUseCase;
import com.cashly.cashly_api.debit_notes.application.usecases.GetDebitNotesUseCase;
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
@RequestMapping("/api/debit-notes")
public class DebitNoteController {

    private final CreateDebitNoteUseCase createDebitNoteUseCase;
    private final GetDebitNotesUseCase getDebitNotesUseCase;
    private final GetDebitNoteByIdUseCase getDebitNoteByIdUseCase;
    private final DeleteDebitNoteUseCase deleteDebitNoteUseCase;

    public DebitNoteController(CreateDebitNoteUseCase createDebitNoteUseCase,
                                GetDebitNotesUseCase getDebitNotesUseCase,
                                GetDebitNoteByIdUseCase getDebitNoteByIdUseCase,
                                DeleteDebitNoteUseCase deleteDebitNoteUseCase) {
        this.createDebitNoteUseCase = createDebitNoteUseCase;
        this.getDebitNotesUseCase = getDebitNotesUseCase;
        this.getDebitNoteByIdUseCase = getDebitNoteByIdUseCase;
        this.deleteDebitNoteUseCase = deleteDebitNoteUseCase;
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
            @RequestParam(required = false) UUID customerId) {
        List<DebitNoteResponse> debitNotes = getDebitNotesUseCase.execute(dateFrom, dateTo, customerId);
        return ResponseEntity.ok(debitNotes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<DebitNoteResponse> getById(@PathVariable UUID id) {
        DebitNoteResponse response = getDebitNoteByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteDebitNoteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

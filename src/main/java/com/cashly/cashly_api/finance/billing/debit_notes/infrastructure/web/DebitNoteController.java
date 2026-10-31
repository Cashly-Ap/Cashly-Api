package com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.web;

import com.cashly.cashly_api.finance.billing.debit_notes.application.create.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.finance.billing.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.finance.billing.debit_notes.application.create.use_case.CreateDebitNoteUseCase;
import com.cashly.cashly_api.finance.billing.debit_notes.application.delete.use_case.DeleteDebitNoteUseCase;
import com.cashly.cashly_api.finance.billing.debit_notes.application.get.use_case.GetDebitNoteByIdUseCase;
import com.cashly.cashly_api.finance.billing.debit_notes.application.list.use_case.GetDebitNotesUseCase;
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
    @PreAuthorize("hasAuthority('FINANCES:CREATE')")
    public ResponseEntity<DebitNoteResponse> create(
            @Valid @RequestBody CreateDebitNoteRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        DebitNoteResponse response = createDebitNoteUseCase.execute(request,
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCES:READ')")
    public ResponseEntity<List<DebitNoteResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId) {
        List<DebitNoteResponse> debitNotes = getDebitNotesUseCase
                .execute(dateFrom, dateTo, customerId);
        return ResponseEntity.ok(debitNotes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCES:READ')")
    public ResponseEntity<DebitNoteResponse> getById(@PathVariable UUID id) {
        DebitNoteResponse response = getDebitNoteByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCES:DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteDebitNoteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

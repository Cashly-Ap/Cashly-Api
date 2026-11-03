package com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.web;

import com.cashly.cashly_api.finance.billing.credit_notes.application.create.request.CreateCreditNoteRequest;
import com.cashly.cashly_api.finance.billing.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.finance.billing.credit_notes.application.create.use_case.CreateCreditNoteUseCase;
import com.cashly.cashly_api.finance.billing.credit_notes.application.delete.use_case.DeleteCreditNoteUseCase;
import com.cashly.cashly_api.finance.billing.credit_notes.application.get.use_case.GetCreditNoteByIdUseCase;
import com.cashly.cashly_api.finance.billing.credit_notes.application.list.use_case.GetCreditNotesUseCase;
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
@RequestMapping("/api/credit-notes")
public class CreditNoteController {

    private final CreateCreditNoteUseCase createCreditNoteUseCase;
    private final GetCreditNotesUseCase getCreditNotesUseCase;
    private final GetCreditNoteByIdUseCase getCreditNoteByIdUseCase;
    private final DeleteCreditNoteUseCase deleteCreditNoteUseCase;

    public CreditNoteController(CreateCreditNoteUseCase createCreditNoteUseCase,
            GetCreditNotesUseCase getCreditNotesUseCase,
            GetCreditNoteByIdUseCase getCreditNoteByIdUseCase,
            DeleteCreditNoteUseCase deleteCreditNoteUseCase) {
        this.createCreditNoteUseCase = createCreditNoteUseCase;
        this.getCreditNotesUseCase = getCreditNotesUseCase;
        this.getCreditNoteByIdUseCase = getCreditNoteByIdUseCase;
        this.deleteCreditNoteUseCase = deleteCreditNoteUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FINANCE_BILLING_CREDIT_NOTES_CREATE')")
    public ResponseEntity<CreditNoteResponse> create(
            @Valid @RequestBody CreateCreditNoteRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CreditNoteResponse response = createCreditNoteUseCase.execute(request,
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_BILLING_CREDIT_NOTES_VIEW_LIST')")
    public ResponseEntity<List<CreditNoteResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId) {
        List<CreditNoteResponse> creditNotes = getCreditNotesUseCase
                .execute(dateFrom, dateTo, customerId);
        return ResponseEntity.ok(creditNotes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_BILLING_CREDIT_NOTES_VIEW_DETAIL')")
    public ResponseEntity<CreditNoteResponse> getById(@PathVariable UUID id) {
        CreditNoteResponse response = getCreditNoteByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_BILLING_CREDIT_NOTES_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCreditNoteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

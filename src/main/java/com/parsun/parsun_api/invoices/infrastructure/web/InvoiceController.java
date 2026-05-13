package com.parsun.parsun_api.invoices.infrastructure.web;

import com.parsun.parsun_api.invoices.application.dto.request.CancelInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.request.UpdateInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.usecases.*;
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
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final GetInvoiceByIdUseCase getInvoiceByIdUseCase;
    private final GetInvoicesUseCase getInvoicesUseCase;
    private final CancelInvoiceUseCase cancelInvoiceUseCase;
    private final DuplicateInvoiceUseCase duplicateInvoiceUseCase;
    private final UpdateInvoiceUseCase updateInvoiceUseCase;
    private final ConfirmInvoiceUseCase confirmInvoiceUseCase;
    private final DeleteInvoiceUseCase deleteInvoiceUseCase;

    public InvoiceController(CreateInvoiceUseCase createInvoiceUseCase,
                              GetInvoiceByIdUseCase getInvoiceByIdUseCase,
                              GetInvoicesUseCase getInvoicesUseCase,
                               CancelInvoiceUseCase cancelInvoiceUseCase,
                               DuplicateInvoiceUseCase duplicateInvoiceUseCase,
                               UpdateInvoiceUseCase updateInvoiceUseCase,
                               ConfirmInvoiceUseCase confirmInvoiceUseCase,
                               DeleteInvoiceUseCase deleteInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.getInvoiceByIdUseCase = getInvoiceByIdUseCase;
        this.getInvoicesUseCase = getInvoicesUseCase;
        this.cancelInvoiceUseCase = cancelInvoiceUseCase;
        this.duplicateInvoiceUseCase = duplicateInvoiceUseCase;
        this.updateInvoiceUseCase = updateInvoiceUseCase;
        this.confirmInvoiceUseCase = confirmInvoiceUseCase;
        this.deleteInvoiceUseCase = deleteInvoiceUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<InvoiceDetailResponse> create(
            @Valid @RequestBody CreateInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        InvoiceDetailResponse response = createInvoiceUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<List<InvoiceListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status) {
        List<InvoiceListResponse> invoices = getInvoicesUseCase.execute(fromDate, toDate, customerId, status);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<InvoiceDetailResponse> getById(@PathVariable UUID id) {
        InvoiceDetailResponse invoice = getInvoiceByIdUseCase.execute(id);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<InvoiceDetailResponse> cancel(@PathVariable UUID id,
                                                         @Valid @RequestBody CancelInvoiceRequest request) {
        InvoiceDetailResponse invoice = cancelInvoiceUseCase.execute(id, request);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<InvoiceDetailResponse> duplicate(@PathVariable UUID id) {
        InvoiceDetailResponse invoice = duplicateInvoiceUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<InvoiceDetailResponse> update(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateInvoiceRequest request) {
        InvoiceDetailResponse response = updateInvoiceUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<InvoiceDetailResponse> confirm(@PathVariable UUID id) {
        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteInvoiceUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

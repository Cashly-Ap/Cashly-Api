package com.cashly.cashly_api.sales.invoices.invoices.infrastructure.web;

import com.cashly.cashly_api.sales.invoices.invoices.application.cancel.request.CancelInvoiceRequest;
import com.cashly.cashly_api.sales.invoices.invoices.application.confirm.request.ConfirmInvoiceRequest;
import com.cashly.cashly_api.sales.invoices.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.sales.invoices.invoices.application.duplicate.request.DuplicateInvoiceRequest;
import com.cashly.cashly_api.sales.invoices.invoices.application.update.request.UpdateInvoiceRequest;
import com.cashly.cashly_api.sales.invoices.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.sales.invoices.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.sales.invoices.invoices.application.cancel.use_case.CancelInvoiceUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.confirm.use_case.ConfirmInvoiceUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.create.use_case.CreateInvoiceUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.delete.use_case.DeleteInvoiceUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.duplicate.use_case.DuplicateInvoiceUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.list.use_case.GetInvoicesUseCase;
import com.cashly.cashly_api.sales.invoices.invoices.application.update.use_case.UpdateInvoiceUseCase;
import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import com.cashly.cashly_api.shared.dto.PageResponse;
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
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_CREATE')")
    public ResponseEntity<InvoiceDetailResponse> create(
            @Valid @RequestBody CreateInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        InvoiceDetailResponse response = createInvoiceUseCase.execute(request,
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_VIEW_LIST')")
    public ResponseEntity<PageResponse<InvoiceListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) List<String> returnStatus,
            @RequestParam(required = false) String dueStatus,
            @RequestParam(required = false) String paymentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir,
            @RequestParam(required = false) DocumentAction action) {
        return ResponseEntity.ok(getInvoicesUseCase.executePage(fromDate,
                toDate, customerId, status, search, returnStatus, dueStatus,
                paymentType, productId, categoryId, page,
                normalizeSize(size), orderBy, orderDir, action));
    }

    public ResponseEntity<PageResponse<InvoiceListResponse>> getAll(
            LocalDate fromDate, LocalDate toDate, UUID customerId,
            String search, List<String> status, UUID productId,
            UUID categoryId, List<String> returnStatus, String dueStatus,
            String orderBy, String orderDir) {
        return getAll(fromDate, toDate, customerId, search, status, productId,
                categoryId, returnStatus, dueStatus, null, 0, 100, orderBy,
                orderDir, null);
    }

    public ResponseEntity<PageResponse<InvoiceListResponse>> getAll(
            LocalDate fromDate, LocalDate toDate, UUID customerId,
            List<String> status, UUID productId, UUID categoryId,
            List<String> returnStatus, String orderBy, String orderDir) {
        return getAll(fromDate, toDate, customerId, null, status, productId,
                categoryId, returnStatus, null, null, 0, 100, orderBy,
                orderDir, null);
    }

    private static int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_VIEW_DETAIL')")
    public ResponseEntity<InvoiceDetailResponse> getById(
            @PathVariable UUID id) {
        InvoiceDetailResponse invoice = getInvoiceByIdUseCase.execute(id);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_CANCEL')")
    public ResponseEntity<InvoiceDetailResponse> cancel(@PathVariable UUID id,
            @Valid @RequestBody CancelInvoiceRequest request) {
        InvoiceDetailResponse invoice = cancelInvoiceUseCase.execute(id,
                request);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_DUPLICATE')")
    public ResponseEntity<InvoiceDetailResponse> duplicate(
            @PathVariable UUID id,
            @RequestBody(required = false) DuplicateInvoiceRequest request) {
        UUID customerId = request != null ? request.customerId() : null;
        InvoiceDetailResponse invoice = duplicateInvoiceUseCase.execute(id,
                customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_EDIT')")
    public ResponseEntity<InvoiceDetailResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        InvoiceDetailResponse response = updateInvoiceUseCase.execute(id,
                request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_CONFIRM')")
    public ResponseEntity<InvoiceDetailResponse> confirm(@PathVariable UUID id,
            @Valid @RequestBody ConfirmInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id,
                request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_INVOICES_INVOICES_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteInvoiceUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

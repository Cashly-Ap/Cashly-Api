package com.cashly.cashly_api.quotes.infrastructure.web;

import com.cashly.cashly_api.quotes.application.dto.request.AssignQuoteWarehousesRequest;
import com.cashly.cashly_api.quotes.application.dto.request.CancelQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.request.ConvertQuoteToInvoiceRequest;
import com.cashly.cashly_api.quotes.application.dto.request.CreateQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.request.UpdateQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.response.*;
import com.cashly.cashly_api.quotes.application.usecases.*;
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
@RequestMapping("/api/quotes")
public class QuoteController {

    private final CreateQuoteUseCase createQuoteUseCase;
    private final GetQuotesUseCase getQuotesUseCase;
    private final GetQuoteByIdUseCase getQuoteByIdUseCase;
    private final UpdateQuoteUseCase updateQuoteUseCase;
    private final DeleteQuoteUseCase deleteQuoteUseCase;
    private final CancelQuoteUseCase cancelQuoteUseCase;
    private final GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase;
    private final AssignQuoteWarehousesUseCase assignQuoteWarehousesUseCase;
    private final ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase;
    private final ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase;

    public QuoteController(CreateQuoteUseCase createQuoteUseCase,
                            GetQuotesUseCase getQuotesUseCase,
                            GetQuoteByIdUseCase getQuoteByIdUseCase,
                            UpdateQuoteUseCase updateQuoteUseCase,
                            DeleteQuoteUseCase deleteQuoteUseCase,
                            CancelQuoteUseCase cancelQuoteUseCase,
                            GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase,
                            AssignQuoteWarehousesUseCase assignQuoteWarehousesUseCase,
                            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
                            ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase) {
        this.createQuoteUseCase = createQuoteUseCase;
        this.getQuotesUseCase = getQuotesUseCase;
        this.getQuoteByIdUseCase = getQuoteByIdUseCase;
        this.updateQuoteUseCase = updateQuoteUseCase;
        this.deleteQuoteUseCase = deleteQuoteUseCase;
        this.cancelQuoteUseCase = cancelQuoteUseCase;
        this.getWarehousesForQuoteUseCase = getWarehousesForQuoteUseCase;
        this.assignQuoteWarehousesUseCase = assignQuoteWarehousesUseCase;
        this.convertQuoteToOrderUseCase = convertQuoteToOrderUseCase;
        this.convertQuoteToInvoiceUseCase = convertQuoteToInvoiceUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<QuoteDetailResponse> create(
            @Valid @RequestBody CreateQuoteRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        QuoteDetailResponse response = createQuoteUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<List<QuoteListResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir) {
        List<QuoteListResponse> quotes = getQuotesUseCase.execute(dateFrom, dateTo, customerId, status, productId, categoryId, orderBy, orderDir);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<QuoteDetailResponse> getById(@PathVariable UUID id) {
        QuoteDetailResponse quote = getQuoteByIdUseCase.execute(id);
        return ResponseEntity.ok(quote);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<QuoteDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuoteRequest request) {
        QuoteDetailResponse response = updateQuoteUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteQuoteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelQuoteRequest request) {
        cancelQuoteUseCase.execute(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/warehouses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<List<QuoteWarehouseResponse>> getWarehouses(
            @RequestParam UUID customerId) {
        List<QuoteWarehouseResponse> result = getWarehousesForQuoteUseCase.execute(customerId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/allocations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<QuoteDetailResponse> assignWarehouses(
            @PathVariable UUID id,
            @Valid @RequestBody AssignQuoteWarehousesRequest request) {
        QuoteDetailResponse response = assignQuoteWarehousesUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<QuoteConvertedToOrderResponse> convertToOrder(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        QuoteConvertedToOrderResponse response = convertQuoteToOrderUseCase.execute(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/convert-to-invoice")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<QuoteConvertedToInvoiceResponse> convertToInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody ConvertQuoteToInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        QuoteConvertedToInvoiceResponse response = convertQuoteToInvoiceUseCase.execute(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

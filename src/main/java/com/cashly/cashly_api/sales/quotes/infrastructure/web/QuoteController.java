package com.cashly.cashly_api.sales.quotes.infrastructure.web;

import com.cashly.cashly_api.sales.quotes.application.cancel.request.CancelQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.request.ConvertQuoteToInvoiceRequest;
import com.cashly.cashly_api.sales.quotes.application.create.request.CreateQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.update.request.UpdateQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.response.QuoteConvertedToInvoiceResponse;
import com.cashly.cashly_api.sales.quotes.application.convert_to_order.response.QuoteConvertedToOrderResponse;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteWarehouseResponse;
import com.cashly.cashly_api.sales.quotes.application.cancel.use_case.CancelQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.use_case.ConvertQuoteToInvoiceUseCase;
import com.cashly.cashly_api.sales.quotes.application.convert_to_order.use_case.ConvertQuoteToOrderUseCase;
import com.cashly.cashly_api.sales.quotes.application.create.use_case.CreateQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.delete.use_case.DeleteQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.get.use_case.GetQuoteByIdUseCase;
import com.cashly.cashly_api.sales.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.sales.quotes.application.warehouses.use_case.GetWarehousesForQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.update.use_case.UpdateQuoteUseCase;
import com.cashly.cashly_api.shared.domain.DocumentAction;
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
    private final ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase;
    private final ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase;

    public QuoteController(CreateQuoteUseCase createQuoteUseCase,
            GetQuotesUseCase getQuotesUseCase,
            GetQuoteByIdUseCase getQuoteByIdUseCase,
            UpdateQuoteUseCase updateQuoteUseCase,
            DeleteQuoteUseCase deleteQuoteUseCase,
            CancelQuoteUseCase cancelQuoteUseCase,
            GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase,
            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
            ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase) {
        this.createQuoteUseCase = createQuoteUseCase;
        this.getQuotesUseCase = getQuotesUseCase;
        this.getQuoteByIdUseCase = getQuoteByIdUseCase;
        this.updateQuoteUseCase = updateQuoteUseCase;
        this.deleteQuoteUseCase = deleteQuoteUseCase;
        this.cancelQuoteUseCase = cancelQuoteUseCase;
        this.getWarehousesForQuoteUseCase = getWarehousesForQuoteUseCase;
        this.convertQuoteToOrderUseCase = convertQuoteToOrderUseCase;
        this.convertQuoteToInvoiceUseCase = convertQuoteToInvoiceUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES:CREATE') and hasAuthority('INVENTORY:READ')")
    public ResponseEntity<QuoteDetailResponse> create(
            @Valid @RequestBody CreateQuoteRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        QuoteDetailResponse response = createQuoteUseCase.execute(request,
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES:READ')")
    public ResponseEntity<List<QuoteListResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir,
            @RequestParam(required = false) DocumentAction action) {
        List<QuoteListResponse> quotes = action == null
                ? getQuotesUseCase.execute(dateFrom, dateTo, customerId, status,
                        productId, categoryId, orderBy, orderDir)
                : getQuotesUseCase.execute(dateFrom, dateTo, customerId, status,
                        productId, categoryId, orderBy, orderDir, action);
        return ResponseEntity.ok(quotes.stream()
                .filter(quote -> matchesSearch(search, quote.quoteNumber(),
                        quote.customerName(), quote.customerRif()))
                .toList());
    }

    private boolean matchesSearch(String search, String... values) {
        if (search == null || search.isBlank())
            return true;
        String term = search.trim().toLowerCase();
        return java.util.Arrays.stream(values)
                .filter(java.util.Objects::nonNull)
                .anyMatch(value -> value.toLowerCase().contains(term));
    }

    public ResponseEntity<List<QuoteListResponse>> list(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, UUID productId,
            UUID categoryId, String orderBy, String orderDir) {
        return list(dateFrom, dateTo, customerId, status, productId, categoryId,
                null, orderBy, orderDir, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES:READ') and hasAuthority('INVENTORY:READ')")
    public ResponseEntity<QuoteDetailResponse> getById(@PathVariable UUID id) {
        QuoteDetailResponse quote = getQuoteByIdUseCase.execute(id);
        return ResponseEntity.ok(quote);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES:UPDATE') and hasAuthority('INVENTORY:READ')")
    public ResponseEntity<QuoteDetailResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateQuoteRequest request) {
        QuoteDetailResponse response = updateQuoteUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES:DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteQuoteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES:EXECUTE')")
    public ResponseEntity<Void> cancel(@PathVariable UUID id,
            @Valid @RequestBody CancelQuoteRequest request) {
        cancelQuoteUseCase.execute(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/warehouses")
    @PreAuthorize("hasAuthority('SALES:READ') and hasAuthority('INVENTORY:READ')")
    public ResponseEntity<List<QuoteWarehouseResponse>> getWarehouses(
            @RequestParam UUID customerId) {
        List<QuoteWarehouseResponse> result = getWarehousesForQuoteUseCase
                .execute(customerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("hasAuthority('SALES:EXECUTE') and hasAuthority('SALES:CREATE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<QuoteConvertedToOrderResponse> convertToOrder(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        QuoteConvertedToOrderResponse response = convertQuoteToOrderUseCase
                .execute(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/convert-to-invoice")
    @PreAuthorize("hasAuthority('SALES:EXECUTE') and hasAuthority('FINANCES:CREATE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<QuoteConvertedToInvoiceResponse> convertToInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody ConvertQuoteToInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        QuoteConvertedToInvoiceResponse response = convertQuoteToInvoiceUseCase
                .execute(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

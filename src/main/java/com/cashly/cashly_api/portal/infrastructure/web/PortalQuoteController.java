package com.cashly.cashly_api.portal.infrastructure.web;

import com.cashly.cashly_api.portal.application.dto.request.CancelPortalQuoteRequest;
import com.cashly.cashly_api.portal.application.dto.request.CreatePortalQuoteRequest;
import com.cashly.cashly_api.portal.application.usecases.CancelPortalQuoteUseCase;
import com.cashly.cashly_api.portal.application.usecases.ConvertPortalQuoteToOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.CreatePortalQuoteUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPortalQuoteUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListPortalQuotesUseCase;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteConvertedToOrderResponse;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal/quotes")
public class PortalQuoteController {

    private final ListPortalQuotesUseCase listPortalQuotesUseCase;
    private final GetPortalQuoteUseCase getPortalQuoteUseCase;
    private final CreatePortalQuoteUseCase createPortalQuoteUseCase;
    private final CancelPortalQuoteUseCase cancelPortalQuoteUseCase;
    private final ConvertPortalQuoteToOrderUseCase convertPortalQuoteToOrderUseCase;

    public PortalQuoteController(ListPortalQuotesUseCase listPortalQuotesUseCase,
                                  GetPortalQuoteUseCase getPortalQuoteUseCase,
                                  CreatePortalQuoteUseCase createPortalQuoteUseCase,
                                  CancelPortalQuoteUseCase cancelPortalQuoteUseCase,
                                  ConvertPortalQuoteToOrderUseCase convertPortalQuoteToOrderUseCase) {
        this.listPortalQuotesUseCase = listPortalQuotesUseCase;
        this.getPortalQuoteUseCase = getPortalQuoteUseCase;
        this.createPortalQuoteUseCase = createPortalQuoteUseCase;
        this.cancelPortalQuoteUseCase = cancelPortalQuoteUseCase;
        this.convertPortalQuoteToOrderUseCase = convertPortalQuoteToOrderUseCase;
    }

    @GetMapping
    public ResponseEntity<List<QuoteListResponse>> listQuotes(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir) {
        List<QuoteListResponse> response = listPortalQuotesUseCase.execute(
                dateFrom, dateTo, status, productId, categoryId, orderBy, orderDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteDetailResponse> getQuote(@PathVariable UUID id) {
        QuoteDetailResponse response = getPortalQuoteUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<QuoteDetailResponse> createQuote(
            @Valid @RequestBody CreatePortalQuoteRequest request) {
        QuoteDetailResponse response = createPortalQuoteUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelQuote(@PathVariable UUID id,
                                             @Valid @RequestBody CancelPortalQuoteRequest request) {
        cancelPortalQuoteUseCase.execute(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<QuoteConvertedToOrderResponse> convertToOrder(@PathVariable UUID id) {
        QuoteConvertedToOrderResponse response = convertPortalQuoteToOrderUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}

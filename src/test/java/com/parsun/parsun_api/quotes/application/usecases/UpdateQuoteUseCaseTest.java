package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.request.CreateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.request.UpdateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateQuoteUseCase Unit Tests")
class UpdateQuoteUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private UpdateQuoteUseCase updateQuoteUseCase;

    @BeforeEach
    void setUp() {
        updateQuoteUseCase = new UpdateQuoteUseCase(quoteRepository, productRepository, warehouseRepository);
    }

    @Test
    @DisplayName("should update draft quote with new lines")
    void shouldUpdateDraftQuote() {
        UUID quoteId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        QuoteLine existingLine = new QuoteLine(UUID.randomUUID(), "Old Product", "OLD-001", BigDecimal.ONE, BigDecimal.TEN);
        Quote quote = Quote.create(
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator",
                LocalDate.now(), "Old notes",
                List.of(existingLine)
        );

        Product product = Product.reconstitute(productId, "PROD-001", "New Product", null, null, false, true, null, BigDecimal.TEN, List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());
        Warehouse warehouse = Warehouse.reconstitute(warehouseId, "WH-001", "Main Warehouse", null, null, null, null, true, LocalDateTime.now(), LocalDateTime.now());

        UpdateQuoteRequest request = new UpdateQuoteRequest(
                null, "Updated notes",
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        productId, BigDecimal.valueOf(2), new BigDecimal("50.0000"),
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.valueOf(2)))
                ))
        );

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteDetailResponse response = updateQuoteUseCase.execute(quoteId, request);

        assertNotNull(response);
        assertEquals(1, response.lines().size());
        assertEquals("New Product", response.lines().get(0).productName());
        verify(quoteRepository).save(quote);
    }

    @Test
    @DisplayName("should throw when trying to update non-draft quote")
    void shouldThrowWhenUpdatingNonDraftQuote() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = Quote.create(
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator",
                LocalDate.now(), null,
                List.of(new QuoteLine(UUID.randomUUID(), "Product", "PROD-001", BigDecimal.ONE, BigDecimal.TEN))
        );
        quote.cancel("reason");

        UpdateQuoteRequest request = new UpdateQuoteRequest(null, null, List.of());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        assertThrows(QuoteStateException.class, () -> updateQuoteUseCase.execute(quoteId, request));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when quote not found")
    void shouldThrowWhenQuoteNotFound() {
        UUID quoteId = UUID.randomUUID();
        UpdateQuoteRequest request = new UpdateQuoteRequest(null, null, List.of());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () -> updateQuoteUseCase.execute(quoteId, request));
    }
}

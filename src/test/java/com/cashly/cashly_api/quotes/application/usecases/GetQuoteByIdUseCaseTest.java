package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cashly.cashly_api.quotes.domain.entities.QuoteLine;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetQuoteByIdUseCase Unit Tests")
class GetQuoteByIdUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private GetQuoteByIdUseCase getQuoteByIdUseCase;

    @BeforeEach
    void setUp() {
        getQuoteByIdUseCase = new GetQuoteByIdUseCase(quoteRepository, warehouseStockRepository, warehouseRepository);
    }

    @Test
    @DisplayName("should return quote detail when quote exists")
    void shouldReturnQuoteDetailWhenExists() {
        UUID quoteId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Quote quote = Quote.reconstitute(
                quoteId,
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator",
                QuoteStatus.DRAFT,
                LocalDate.now(), null, BigDecimal.TEN,
                null, null, null, null, null, null,
                List.of(new QuoteLine(productId, "Product", "PROD-001", BigDecimal.ONE, BigDecimal.TEN)),
                Instant.now(), Instant.now()
        );

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(warehouseStockRepository.findByProductId(productId)).thenReturn(List.of());

        QuoteDetailResponse response = getQuoteByIdUseCase.execute(quoteId);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        verify(quoteRepository).findById(quoteId);
    }

    @Test
    @DisplayName("should throw QuoteNotFoundException when quote does not exist")
    void shouldThrowWhenQuoteNotFound() {
        UUID quoteId = UUID.randomUUID();
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () -> getQuoteByIdUseCase.execute(quoteId));
    }
}

package com.cashly.cashly_api.quotes.application.get.use_case;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.domain.entity.Quote;
import com.cashly.cashly_api.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetQuoteByIdUseCaseTest {
    @Mock QuoteRepository quotes;
    @Mock QuoteWarehouseGateway warehouses;
    private GetQuoteByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetQuoteByIdUseCase(quotes, warehouses);
    }

    @Test
    void shouldReturnQuoteDetailAndWarehouseStockFromGateway() {
        UUID quoteId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Quote quote = Quote.reconstitute(quoteId, QuoteNumber.of(2026, 1), UUID.randomUUID(), "Customer", "RIF",
                UUID.randomUUID(), "Actor", QuoteStatus.DRAFT, LocalDate.now(), null, BigDecimal.TEN,
                null, null, null, null, null, null,
                List.of(new QuoteLine(productId, "Product", "P-1", BigDecimal.ONE, BigDecimal.TEN)),
                Instant.now(), Instant.now());
        when(quotes.findById(quoteId)).thenReturn(Optional.of(quote));
        when(warehouses.findStockByProductId(productId)).thenReturn(List.of(
                new QuoteWarehouseGateway.Stock(warehouseId, "Main", 7)));

        var response = useCase.execute(quoteId);

        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.lines()).singleElement().satisfies(line ->
                assertThat(line.warehouseStocks()).singleElement().satisfies(stock -> {
                    assertThat(stock.warehouseId()).isEqualTo(warehouseId);
                    assertThat(stock.warehouseName()).isEqualTo("Main");
                    assertThat(stock.available()).isEqualTo(7);
                }));
    }

    @Test
    void shouldThrowWhenQuoteDoesNotExist() {
        UUID quoteId = UUID.randomUUID();
        when(quotes.findById(quoteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(quoteId)).isInstanceOf(QuoteNotFoundException.class);
        verifyNoInteractions(warehouses);
    }
}

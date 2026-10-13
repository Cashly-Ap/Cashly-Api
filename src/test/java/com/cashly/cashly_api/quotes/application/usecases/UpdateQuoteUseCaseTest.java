package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.dto.request.CreateQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.request.UpdateQuoteRequest;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.entities.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateQuoteUseCaseTest {
    @Mock QuoteRepository quotes;
    @Mock QuoteProductCatalogGateway products;
    @Mock QuoteWarehouseGateway warehouses;
    private UpdateQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateQuoteUseCase(quotes, products, warehouses);
    }

    @Test
    void shouldReplaceDraftLinesThroughContractsAndPreserveCategorySnapshot() {
        UUID quoteId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Quote quote = Quote.create(QuoteNumber.of(2026, 1), UUID.randomUUID(), "Acme", "RIF",
                UUID.randomUUID(), "Actor", LocalDate.now(), null,
                List.of(new QuoteLine(UUID.randomUUID(), "Old", "OLD", BigDecimal.ONE, BigDecimal.TEN)));
        AtomicReference<Quote> saved = new AtomicReference<>();
        when(quotes.findById(quoteId)).thenReturn(Optional.of(quote));
        when(products.findProduct(productId)).thenReturn(Optional.of(
                new QuoteProductCatalogGateway.ProductReference(productId, "P-1", "Product", categoryId)));
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(
                new QuoteWarehouseGateway.Warehouse(warehouseId, "Main", "Address")));
        when(quotes.save(any(Quote.class))).thenAnswer(invocation -> {
            Quote value = invocation.getArgument(0);
            saved.set(value);
            return value;
        });
        var request = new UpdateQuoteRequest(null, "New notes", List.of(
                new CreateQuoteRequest.QuoteLineRequest(productId, BigDecimal.valueOf(2), BigDecimal.TEN, List.of(
                        new CreateQuoteRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.valueOf(2))))));

        var response = useCase.execute(quoteId, request);

        assertThat(response.lines()).singleElement().extracting("productName").isEqualTo("Product");
        assertThat(saved.get().getLines()).singleElement().extracting("categoryId").isEqualTo(categoryId);
    }

    @Test
    void shouldRejectUpdateWhenQuoteIsNotDraft() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = Quote.create(QuoteNumber.of(2026, 1), UUID.randomUUID(), "Acme", "RIF",
                UUID.randomUUID(), "Actor", LocalDate.now(), null, List.of(new QuoteLine(
                        UUID.randomUUID(), "Product", "P-1", BigDecimal.ONE, BigDecimal.TEN)));
        quote.cancel("reason");
        when(quotes.findById(quoteId)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> useCase.execute(quoteId, new UpdateQuoteRequest(null, null, List.of())))
                .isInstanceOf(QuoteStateException.class);
        verify(quotes, never()).save(any());
    }

    @Test
    void shouldThrowWhenQuoteDoesNotExist() {
        UUID quoteId = UUID.randomUUID();
        when(quotes.findById(quoteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(quoteId, new UpdateQuoteRequest(null, null, List.of())))
                .isInstanceOf(QuoteNotFoundException.class);
    }
}

package com.cashly.cashly_api.sales.quotes.application.list.use_case;

import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetQuotesUseCase Unit Tests")
class GetQuotesUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteProductCatalogGateway products;

    private GetQuotesUseCase getQuotesUseCase;

    @BeforeEach
    void setUp() {
        getQuotesUseCase = new GetQuotesUseCase(quoteRepository, products);
    }

    @Test
    @DisplayName("should return list of quotes mapped to list responses")
    void shouldReturnQuoteListResponses() {
        Quote quote = Quote.reconstitute(
                UUID.randomUUID(),
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator",
                QuoteStatus.DRAFT,
                LocalDate.of(2026, 12, 31), "Notes", BigDecimal.TEN,
                null, null, null, null, null, null,
                List.of(new QuoteLine(UUID.randomUUID(), "Product", "PROD-001", BigDecimal.ONE, BigDecimal.TEN)),
                Instant.now(), Instant.now()
        );

        when(quoteRepository.findAllWithFilters(null, null, null, null, null, null, java.util.Set.of(), "createdAt", "desc"))
                .thenReturn(List.of(quote));

        List<QuoteListResponse> responses = getQuotesUseCase.execute(
                null, null, null, null, null, null, "createdAt", "desc");

        assertEquals(1, responses.size());
        QuoteListResponse response = responses.get(0);
        assertEquals(quote.getId(), response.id());
        assertEquals("PRE-2026-0001", response.quoteNumber());
        assertEquals("Customer", response.customerName());
        assertEquals("DRAFT", response.status());
        assertEquals(quote.getCreatedByName(), response.createdByName());
        verify(quoteRepository).findAllWithFilters(null, null, null, null, null, null, java.util.Set.of(), "createdAt", "desc");
    }

    @Test
    @DisplayName("should return empty list when no quotes exist")
    void shouldReturnEmptyListWhenNoQuotes() {
        when(quoteRepository.findAllWithFilters(null, null, null, null, null, null, java.util.Set.of(), "createdAt", "desc"))
                .thenReturn(List.of());

        List<QuoteListResponse> responses = getQuotesUseCase.execute(
                null, null, null, null, null, null, "createdAt", "desc");

        assertTrue(responses.isEmpty());
    }
}

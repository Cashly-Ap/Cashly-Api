package com.cashly.cashly_api.sales.quotes.application.delete.use_case;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteStateException;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteQuoteUseCase Unit Tests")
class DeleteQuoteUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    private DeleteQuoteUseCase deleteQuoteUseCase;

    @BeforeEach
    void setUp() {
        deleteQuoteUseCase = new DeleteQuoteUseCase(quoteRepository, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
    }

    @Test
    @DisplayName("should delete draft quote")
    void shouldDeleteDraftQuote() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = createDraftQuote(quoteId);

        when(quoteRepository.findEligibleForDeletion(quoteId))
                .thenReturn(Optional.of(quote));

        deleteQuoteUseCase.execute(quoteId);

        verify(quoteRepository).delete(quoteId);
    }

    @Test
    @DisplayName("should throw when trying to delete non-draft quote")
    void shouldThrowWhenDeletingNonDraftQuote() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = createDraftQuote(quoteId);
        quote.cancel("reason");

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        assertThrows(QuoteStateException.class,
                () -> deleteQuoteUseCase.execute(quoteId));
        verify(quoteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should throw when quote not found")
    void shouldThrowWhenQuoteNotFound() {
        UUID quoteId = UUID.randomUUID();
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class,
                () -> deleteQuoteUseCase.execute(quoteId));
    }

    private Quote createDraftQuote(UUID quoteId) {
        return Quote.reconstitute(quoteId, QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator", QuoteStatus.DRAFT,
                LocalDate.now(), null, BigDecimal.TEN, null, null, null, null,
                null, null,
                List.of(new QuoteLine(UUID.randomUUID(), "Product", "PROD-001",
                        BigDecimal.ONE, BigDecimal.TEN)),
                Instant.now(), Instant.now());
    }
}

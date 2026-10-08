package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.request.CancelQuoteRequest;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelQuoteUseCase Unit Tests")
class CancelQuoteUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private NotificationService notificationService;

    private CancelQuoteUseCase cancelQuoteUseCase;

    @BeforeEach
    void setUp() {
        cancelQuoteUseCase = new CancelQuoteUseCase(quoteRepository,
                new com.cashly.cashly_api.quotes.infrastructure.integration.LocalQuoteRejectionNotificationGateway(
                        new com.cashly.cashly_api.notifications.application.services.QuoteRejectionNotificationService(notificationService)));
    }

    @Test
    @DisplayName("should cancel draft quote with reason")
    void shouldCancelDraftQuote() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = createDraftQuote(quoteId);

        CancelQuoteRequest request = new CancelQuoteRequest("Customer decided to cancel");

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        cancelQuoteUseCase.execute(quoteId, request);

        assertEquals("CANCELLED", quote.getStatus().name());
        assertNotNull(quote.getCancelledAt());
        assertEquals("Customer decided to cancel", quote.getCancelledReason());
        verify(quoteRepository).save(quote);
        var sequence = inOrder(quoteRepository, notificationService);
        sequence.verify(quoteRepository).save(quote);
        sequence.verify(notificationService).notifyCustomerQuoteRejected(quote.getCustomerId(), quoteId, request.reason());
    }

    @Test
    @DisplayName("should throw when trying to cancel non-DRAFT quote")
    void shouldThrowWhenCancellingNonDraftQuote() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = createDraftQuote(quoteId);
        quote.markConvertedToOrder(UUID.randomUUID(), "ORD-001");

        CancelQuoteRequest request = new CancelQuoteRequest("Customer no longer interested");

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        assertThrows(com.cashly.cashly_api.quotes.domain.exceptions.QuoteStateException.class,
                () -> cancelQuoteUseCase.execute(quoteId, request));
        verify(quoteRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("should throw when quote not found")
    void shouldThrowWhenQuoteNotFound() {
        UUID quoteId = UUID.randomUUID();
        CancelQuoteRequest request = new CancelQuoteRequest("reason");

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () -> cancelQuoteUseCase.execute(quoteId, request));
        verifyNoInteractions(notificationService);
    }

    private Quote createDraftQuote(UUID quoteId) {
        return Quote.reconstitute(
                quoteId,
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator",
                QuoteStatus.DRAFT,
                LocalDate.now(), null, BigDecimal.TEN,
                null, null, null, null, null, null,
                List.of(new QuoteLine(UUID.randomUUID(), "Product", "PROD-001", BigDecimal.ONE, BigDecimal.TEN)),
                Instant.now(), Instant.now()
        );
    }
}

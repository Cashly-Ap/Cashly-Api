package com.cashly.cashly_api.reports.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;
import com.cashly.cashly_api.reports.quotes.application.port.QuotePdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateQuotePdfUseCaseImpl Unit Tests")
class GenerateQuotePdfUseCaseImplTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuotePdfPort quotePdfPort;

    @InjectMocks
    private GenerateQuotePdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate quote PDF for given quote ID")
    void should_generate_quote_pdf() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = mock(Quote.class);
        when(quote.getId()).thenReturn(quoteId);
        when(quote.getQuoteNumber()).thenReturn(QuoteNumber.of(2026, 1));
        when(quote.getCustomerId()).thenReturn(UUID.randomUUID());
        when(quote.getCustomerName()).thenReturn("Customer A");
        when(quote.getCustomerRif()).thenReturn("J-12345678");
        when(quote.getCreatedBy()).thenReturn(UUID.randomUUID());
        when(quote.getCreatedByName()).thenReturn("Sales User");
        when(quote.getStatus()).thenReturn(QuoteStatus.DRAFT);
        when(quote.getExpiryDate()).thenReturn(LocalDate.now().plusDays(30));
        when(quote.getNotes()).thenReturn("Some notes");
        when(quote.getSubtotal()).thenReturn(BigDecimal.valueOf(500));
        when(quote.getOrderId()).thenReturn(null);
        when(quote.getOrderNumber()).thenReturn(null);
        when(quote.getInvoiceId()).thenReturn(null);
        when(quote.getInvoiceNumber()).thenReturn(null);
        when(quote.getCancelledAt()).thenReturn(null);
        when(quote.getCancelledReason()).thenReturn(null);
        when(quote.getCreatedAt()).thenReturn(Instant.now());
        when(quote.getUpdatedAt()).thenReturn(Instant.now());
        when(quote.getLines()).thenReturn(List.of());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        byte[] expectedPdf = "Quote PDF".getBytes();
        when(quotePdfPort.generatePdf(any(QuoteDetailResponse.class))).thenReturn(expectedPdf);

        byte[] result = useCase.execute(quoteId);

        assertArrayEquals(expectedPdf, result);
        verify(quoteRepository).findById(quoteId);
        verify(quotePdfPort).generatePdf(any(QuoteDetailResponse.class));
    }

    @Test
    @DisplayName("should throw QuoteNotFoundException when quote not found")
    void should_throw_when_quote_not_found() {
        UUID quoteId = UUID.randomUUID();

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () -> useCase.execute(quoteId));
        verify(quoteRepository).findById(quoteId);
        verifyNoInteractions(quotePdfPort);
    }
}

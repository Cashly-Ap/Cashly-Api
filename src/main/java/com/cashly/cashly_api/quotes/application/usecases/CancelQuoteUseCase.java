package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.contracts.QuoteRejectionNotificationGateway;
import com.cashly.cashly_api.quotes.application.dto.request.CancelQuoteRequest;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteRejectionNotificationGateway notificationService;

    public CancelQuoteUseCase(QuoteRepository quoteRepository, QuoteRejectionNotificationGateway notificationService) {
        this.quoteRepository = quoteRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void execute(UUID id, CancelQuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        quote.cancel(request.reason());
        quoteRepository.save(quote);
        notificationService.rejected(quote.getCustomerId(), id, request.reason());
    }
}

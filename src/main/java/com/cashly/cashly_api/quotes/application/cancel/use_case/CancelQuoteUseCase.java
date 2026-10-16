package com.cashly.cashly_api.quotes.application.cancel.use_case;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteRejectionNotificationGateway;
import com.cashly.cashly_api.quotes.application.cancel.request.CancelQuoteRequest;
import com.cashly.cashly_api.quotes.domain.entity.Quote;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
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

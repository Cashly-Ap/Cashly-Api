package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.request.CancelQuoteRequest;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelQuoteUseCase {

    private final QuoteRepository quoteRepository;

    public CancelQuoteUseCase(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public void execute(UUID id, CancelQuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        quote.cancel(request.reason());
        quoteRepository.save(quote);
    }
}

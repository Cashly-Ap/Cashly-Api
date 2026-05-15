package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteQuoteUseCase {

    private final QuoteRepository quoteRepository;

    public DeleteQuoteUseCase(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        if (!quote.canBeModified()) {
            throw new QuoteStateException("Quote can only be deleted in DRAFT status");
        }

        quoteRepository.delete(id);
    }
}

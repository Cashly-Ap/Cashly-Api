package com.cashly.cashly_api.quotes.application.delete.use_case;

import com.cashly.cashly_api.quotes.domain.entity.Quote;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
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

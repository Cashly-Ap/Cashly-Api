package com.cashly.cashly_api.sales.quotes.application.delete.use_case;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteQuoteUseCase {

    private final QuoteRepository quoteRepository;

    public DeleteQuoteUseCase(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Quote quote = quoteRepository.findEligibleForDeletion(id)
                .or(() -> quoteRepository.findById(id))
                .orElseThrow(() -> new QuoteNotFoundException(id));

        if (!quote.canBeModified()) {
            throw new QuoteStateException(
                    "Quote can only be deleted in DRAFT status");
        }

        if (quoteRepository.findEligibleForDeletion(id).isEmpty()) {
            throw new QuoteStateException(
                    "Quote can only be deleted when it is not linked to an invoice");
        }

        quoteRepository.delete(id);
    }
}

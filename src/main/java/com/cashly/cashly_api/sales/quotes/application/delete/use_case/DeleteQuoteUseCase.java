package com.cashly.cashly_api.sales.quotes.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class DeleteQuoteUseCase {

    private final QuoteRepository quoteRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteQuoteUseCase(QuoteRepository quoteRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    @Audited(module = "sales", entityType = "QUOTE", action = "DELETE")
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

        activityNotifications.record(ActivityEvent.of("SALES_QUOTES",
                "DELETE", "QUOTE", quote.getId(),
                quote.getQuoteNumber().value(), null));
        quoteRepository.delete(id);
    }
}

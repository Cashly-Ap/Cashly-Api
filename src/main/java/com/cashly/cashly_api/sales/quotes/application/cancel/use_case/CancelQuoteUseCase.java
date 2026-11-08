package com.cashly.cashly_api.sales.quotes.application.cancel.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.quotes.application.cancel.request.CancelQuoteRequest;
import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class CancelQuoteUseCase {

    private final QuoteRepository quoteRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public CancelQuoteUseCase(QuoteRepository quoteRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    @Audited(module = "sales", entityType = "QUOTE", action = "CANCEL")
    public void execute(UUID id, CancelQuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        quote.cancel(request.reason());
        quoteRepository.save(quote);
        activityNotifications.record(ActivityEvent.of("SALES_QUOTES",
                "CANCEL", "QUOTE", quote.getId(),
                quote.getQuoteNumber().value(), null));
    }
}

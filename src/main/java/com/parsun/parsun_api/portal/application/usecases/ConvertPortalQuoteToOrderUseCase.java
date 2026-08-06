package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToOrderResponse;
import com.parsun.parsun_api.quotes.application.usecases.ConvertQuoteToOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ConvertPortalQuoteToOrderUseCase {

    private final ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase;

    public ConvertPortalQuoteToOrderUseCase(ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase) {
        this.convertQuoteToOrderUseCase = convertQuoteToOrderUseCase;
    }

    @Transactional
    public QuoteConvertedToOrderResponse execute(UUID quoteId) {
        return convertQuoteToOrderUseCase.execute(quoteId, null);
    }
}

package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToOrderResponse;
import com.parsun.parsun_api.quotes.application.usecases.ConvertQuoteToOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ConvertPortalQuoteToOrderUseCase {

    private final ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase;
    private final GetPortalQuoteUseCase getPortalQuoteUseCase;

    public ConvertPortalQuoteToOrderUseCase(ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
                                            GetPortalQuoteUseCase getPortalQuoteUseCase) {
        this.convertQuoteToOrderUseCase = convertQuoteToOrderUseCase;
        this.getPortalQuoteUseCase = getPortalQuoteUseCase;
    }

    @Transactional
    public QuoteConvertedToOrderResponse execute(UUID quoteId) {
        getPortalQuoteUseCase.execute(quoteId);
        return convertQuoteToOrderUseCase.execute(quoteId, null);
    }
}

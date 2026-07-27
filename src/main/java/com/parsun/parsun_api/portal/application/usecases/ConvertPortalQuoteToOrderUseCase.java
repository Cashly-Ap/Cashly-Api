package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToOrderResponse;
import com.parsun.parsun_api.quotes.application.usecases.ConvertQuoteToOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ConvertPortalQuoteToOrderUseCase {

    private final ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public ConvertPortalQuoteToOrderUseCase(ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
                                             PortalAuthorizationService portalAuthorizationService) {
        this.convertQuoteToOrderUseCase = convertQuoteToOrderUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional
    public QuoteConvertedToOrderResponse execute(UUID quoteId) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        return convertQuoteToOrderUseCase.execute(quoteId, customerId);
    }
}

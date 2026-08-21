package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.usecases.GetQuoteByIdUseCase;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalQuoteUseCase {

    private final GetQuoteByIdUseCase getQuoteByIdUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public GetPortalQuoteUseCase(GetQuoteByIdUseCase getQuoteByIdUseCase,
                                PortalAuthorizationService portalAuthorizationService) {
        this.getQuoteByIdUseCase = getQuoteByIdUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public QuoteDetailResponse execute(UUID id) {
        var quote = getQuoteByIdUseCase.execute(id);
        portalAuthorizationService.requireOwnership(quote.customerId(), "Quote");
        return quote;
    }
}

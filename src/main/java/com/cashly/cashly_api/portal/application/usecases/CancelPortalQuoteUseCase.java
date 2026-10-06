package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.portal.application.dto.request.CancelPortalQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.request.CancelQuoteRequest;
import com.cashly.cashly_api.quotes.application.usecases.CancelQuoteUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class CancelPortalQuoteUseCase {

    private final CancelQuoteUseCase cancelQuoteUseCase;
    private final GetPortalQuoteUseCase getPortalQuoteUseCase;

    public CancelPortalQuoteUseCase(CancelQuoteUseCase cancelQuoteUseCase,
                                    GetPortalQuoteUseCase getPortalQuoteUseCase) {
        this.cancelQuoteUseCase = cancelQuoteUseCase;
        this.getPortalQuoteUseCase = getPortalQuoteUseCase;
    }

    @Transactional
    public void execute(UUID id, CancelPortalQuoteRequest request) {
        getPortalQuoteUseCase.execute(id);
        cancelQuoteUseCase.execute(id, new CancelQuoteRequest(request.reason()));
    }
}

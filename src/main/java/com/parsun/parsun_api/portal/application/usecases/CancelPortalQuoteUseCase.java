package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.portal.application.dto.request.CancelPortalQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.request.CancelQuoteRequest;
import com.parsun.parsun_api.quotes.application.usecases.CancelQuoteUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class CancelPortalQuoteUseCase {

    private final CancelQuoteUseCase cancelQuoteUseCase;

    public CancelPortalQuoteUseCase(CancelQuoteUseCase cancelQuoteUseCase) {
        this.cancelQuoteUseCase = cancelQuoteUseCase;
    }

    @Transactional
    public void execute(UUID id, CancelPortalQuoteRequest request) {
        cancelQuoteUseCase.execute(id, new CancelQuoteRequest(request.reason()));
    }
}

package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.usecases.GetQuoteByIdUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalQuoteUseCase {

    private final GetQuoteByIdUseCase getQuoteByIdUseCase;

    public GetPortalQuoteUseCase(GetQuoteByIdUseCase getQuoteByIdUseCase) {
        this.getQuoteByIdUseCase = getQuoteByIdUseCase;
    }

    @Transactional(readOnly = true)
    public QuoteDetailResponse execute(UUID id) {
        return getQuoteByIdUseCase.execute(id);
    }
}

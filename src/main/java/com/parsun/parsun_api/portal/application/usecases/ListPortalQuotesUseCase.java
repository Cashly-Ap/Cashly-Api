package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;
import com.parsun.parsun_api.quotes.application.usecases.GetQuotesUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ListPortalQuotesUseCase {

    private final GetQuotesUseCase getQuotesUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public ListPortalQuotesUseCase(GetQuotesUseCase getQuotesUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.getQuotesUseCase = getQuotesUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<QuoteListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                            String status, UUID productId,
                                            UUID categoryId, String orderBy,
                                            String orderDir) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        return getQuotesUseCase.execute(dateFrom, dateTo, customerId, status,
                productId, categoryId, orderBy, orderDir);
    }
}

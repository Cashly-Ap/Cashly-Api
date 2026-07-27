package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoicesUseCase;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ListPortalInvoicesUseCase {

    private final GetInvoicesUseCase getInvoicesUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public ListPortalInvoicesUseCase(GetInvoicesUseCase getInvoicesUseCase,
                                      PortalAuthorizationService portalAuthorizationService) {
        this.getInvoicesUseCase = getInvoicesUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<InvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              List<String> statuses,
                                              List<String> returnStatus,
                                              UUID productId, UUID categoryId,
                                              String orderBy, String orderDir) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        return getInvoicesUseCase.execute(dateFrom, dateTo, customerId, statuses,
                returnStatus, productId, categoryId, orderBy, orderDir);
    }
}

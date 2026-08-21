package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalInvoiceUseCase {

    private final GetInvoiceByIdUseCase getInvoiceByIdUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public GetPortalInvoiceUseCase(GetInvoiceByIdUseCase getInvoiceByIdUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.getInvoiceByIdUseCase = getInvoiceByIdUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public InvoiceDetailResponse execute(UUID id) {
        InvoiceDetailResponse invoice = getInvoiceByIdUseCase.execute(id);

        portalAuthorizationService.requireOwnership(invoice.customerId(), "Invoice");

        return invoice;
    }
}

package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalInvoiceUseCase {

    private final GetInvoiceByIdUseCase getInvoiceByIdUseCase;

    public GetPortalInvoiceUseCase(GetInvoiceByIdUseCase getInvoiceByIdUseCase) {
        this.getInvoiceByIdUseCase = getInvoiceByIdUseCase;
    }

    @Transactional(readOnly = true)
    public InvoiceDetailResponse execute(UUID id) {
        return getInvoiceByIdUseCase.execute(id);
    }
}

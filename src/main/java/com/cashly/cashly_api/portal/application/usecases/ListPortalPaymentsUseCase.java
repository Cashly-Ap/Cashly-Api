package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.payments.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.application.usecases.GetAllPaymentsUseCase;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public class ListPortalPaymentsUseCase {

    private final GetAllPaymentsUseCase getAllPaymentsUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public ListPortalPaymentsUseCase(GetAllPaymentsUseCase getAllPaymentsUseCase,
                                      PortalAuthorizationService portalAuthorizationService) {
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<PaymentListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              String type, String status) {
        var customerId = portalAuthorizationService.getCurrentCustomerId();
        return getAllPaymentsUseCase.execute(dateFrom, dateTo, customerId, type, status);
    }
}

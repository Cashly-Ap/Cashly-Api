package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.application.usecases.GetOrdersUseCase;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ListPortalOrdersUseCase {

    private final GetOrdersUseCase getOrdersUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public ListPortalOrdersUseCase(GetOrdersUseCase getOrdersUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.getOrdersUseCase = getOrdersUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<OrderListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                            String status, UUID productId,
                                            UUID categoryId, String orderBy,
                                            String orderDir) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        return getOrdersUseCase.execute(dateFrom, dateTo, customerId, status,
                productId, categoryId, orderBy, orderDir);
    }
}

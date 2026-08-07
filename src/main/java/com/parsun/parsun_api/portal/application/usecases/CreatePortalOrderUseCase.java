package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.orders.application.dto.request.CreateOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.usecases.CreateOrderUseCase;
import com.parsun.parsun_api.portal.application.dto.request.CreatePortalOrderRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class CreatePortalOrderUseCase {

    private final CreateOrderUseCase createOrderUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public CreatePortalOrderUseCase(CreateOrderUseCase createOrderUseCase,
                                     PortalAuthorizationService portalAuthorizationService) {
        this.createOrderUseCase = createOrderUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional
    public OrderDetailResponse execute(CreatePortalOrderRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

        CreateOrderRequest internalRequest = new CreateOrderRequest(
                customerId,
                request.notes(),
                request.lines().stream()
                        .map(line -> new CreateOrderRequest.OrderLineRequest(
                                line.productId(),
                                line.quantity(),
                                line.unitPrice(),
                                List.of()
                        ))
                        .toList()
        );

        return createOrderUseCase.executeCustomerDraft(internalRequest);
    }
}

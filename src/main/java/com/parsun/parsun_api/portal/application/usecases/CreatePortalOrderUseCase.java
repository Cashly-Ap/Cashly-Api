package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.notifications.application.services.NotificationService;
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
    private final NotificationService notificationService;

    public CreatePortalOrderUseCase(CreateOrderUseCase createOrderUseCase,
                                     PortalAuthorizationService portalAuthorizationService,
                                     NotificationService notificationService) {
        this.createOrderUseCase = createOrderUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderDetailResponse execute(CreatePortalOrderRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        request.lines().forEach(line -> portalAuthorizationService.requireProductAccess(line.productId()));

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

        OrderDetailResponse response = createOrderUseCase.executeCustomerDraft(internalRequest);
        notificationService.notifyAdminsOrderCreated(response.id(), response.customerName());
        return response;
    }
}

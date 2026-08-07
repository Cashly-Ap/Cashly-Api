package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.orders.application.dto.request.UpdateOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.usecases.UpdateOrderUseCase;
import com.parsun.parsun_api.portal.application.dto.request.UpdatePortalOrderRequest;

import java.util.List;
import java.util.UUID;

public class UpdatePortalOrderUseCase {

    private final UpdateOrderUseCase updateOrderUseCase;

    public UpdatePortalOrderUseCase(UpdateOrderUseCase updateOrderUseCase) {
        this.updateOrderUseCase = updateOrderUseCase;
    }

    public OrderDetailResponse execute(UUID id, UpdatePortalOrderRequest request) {
        UpdateOrderRequest internalRequest = new UpdateOrderRequest(
                request.notes(),
                request.lines().stream()
                        .map(line -> new UpdateOrderRequest.OrderLineRequest(
                                line.productId(),
                                line.quantity(),
                                line.unitPrice(),
                                List.of()
                        ))
                        .toList()
        );

        return updateOrderUseCase.execute(id, internalRequest);
    }
}

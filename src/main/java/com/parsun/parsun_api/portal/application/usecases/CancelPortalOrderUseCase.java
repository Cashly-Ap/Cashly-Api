package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.orders.application.dto.request.CancelOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.usecases.CancelOrderUseCase;
import com.parsun.parsun_api.portal.application.dto.request.CancelPortalOrderRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class CancelPortalOrderUseCase {

    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetPortalOrderUseCase getPortalOrderUseCase;

    public CancelPortalOrderUseCase(CancelOrderUseCase cancelOrderUseCase,
                                    GetPortalOrderUseCase getPortalOrderUseCase) {
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getPortalOrderUseCase = getPortalOrderUseCase;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id, CancelPortalOrderRequest request) {
        getPortalOrderUseCase.execute(id);
        return cancelOrderUseCase.execute(id, new CancelOrderRequest(request.reason()));
    }
}

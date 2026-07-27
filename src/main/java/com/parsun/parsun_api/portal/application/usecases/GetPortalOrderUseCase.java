package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.usecases.GetOrderByIdUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalOrderUseCase {

    private final GetOrderByIdUseCase getOrderByIdUseCase;

    public GetPortalOrderUseCase(GetOrderByIdUseCase getOrderByIdUseCase) {
        this.getOrderByIdUseCase = getOrderByIdUseCase;
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse execute(UUID id) {
        return getOrderByIdUseCase.execute(id);
    }
}

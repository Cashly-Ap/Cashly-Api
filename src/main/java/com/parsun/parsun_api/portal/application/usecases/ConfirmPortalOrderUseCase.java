package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.usecases.ConfirmOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ConfirmPortalOrderUseCase {

    private final ConfirmOrderUseCase confirmOrderUseCase;

    public ConfirmPortalOrderUseCase(ConfirmOrderUseCase confirmOrderUseCase) {
        this.confirmOrderUseCase = confirmOrderUseCase;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id) {
        return confirmOrderUseCase.execute(id);
    }
}

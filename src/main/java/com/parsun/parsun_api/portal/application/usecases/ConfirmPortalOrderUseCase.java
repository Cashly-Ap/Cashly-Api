package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.usecases.ConfirmOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ConfirmPortalOrderUseCase {

    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final GetPortalOrderUseCase getPortalOrderUseCase;

    public ConfirmPortalOrderUseCase(ConfirmOrderUseCase confirmOrderUseCase,
                                    GetPortalOrderUseCase getPortalOrderUseCase) {
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.getPortalOrderUseCase = getPortalOrderUseCase;
    }

    @Transactional
    public OrderDetailResponse execute(UUID id) {
        getPortalOrderUseCase.execute(id);
        return confirmOrderUseCase.execute(id);
    }
}

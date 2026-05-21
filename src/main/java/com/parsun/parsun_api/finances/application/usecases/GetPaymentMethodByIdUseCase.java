package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;

import java.util.UUID;

public class GetPaymentMethodByIdUseCase {

    private final PaymentMethodRepository repository;

    public GetPaymentMethodByIdUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    public PaymentMethodResponse execute(UUID id) {
        PaymentMethod paymentMethod = repository.findById(PaymentMethodId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + id));

        return FinanceMapper.toPaymentMethodResponse(paymentMethod);
    }
}

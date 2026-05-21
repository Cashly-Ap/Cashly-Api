package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.UpdatePaymentMethodRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class UpdatePaymentMethodUseCase {

    private final PaymentMethodRepository repository;

    public UpdatePaymentMethodUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentMethodResponse execute(UUID id, UpdatePaymentMethodRequest request) {
        PaymentMethod paymentMethod = repository.findById(PaymentMethodId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + id));

        paymentMethod.update(request.name(), request.description());
        PaymentMethod saved = repository.save(paymentMethod);

        return FinanceMapper.toPaymentMethodResponse(saved);
    }
}

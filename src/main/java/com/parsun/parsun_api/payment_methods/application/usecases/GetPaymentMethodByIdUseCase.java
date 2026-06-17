package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetPaymentMethodByIdUseCase {

    private final PaymentMethodRepository repository;

    public GetPaymentMethodByIdUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    public PaymentMethodResponse execute(UUID id) {
        PaymentMethod paymentMethod = repository.findById(PaymentMethodId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + id));

        return PaymentMethodMapper.toResponse(paymentMethod);
    }
}

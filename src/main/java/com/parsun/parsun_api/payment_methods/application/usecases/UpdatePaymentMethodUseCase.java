package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.request.UpdatePaymentMethodRequest;
import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdatePaymentMethodUseCase {

    private final PaymentMethodRepository repository;

    public UpdatePaymentMethodUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentMethodResponse execute(UUID id, UpdatePaymentMethodRequest request) {
        PaymentMethod paymentMethod = repository.findById(PaymentMethodId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + id));

        paymentMethod.update(request.name(), request.description(), request.isActive());
        PaymentMethod saved = repository.save(paymentMethod);

        return PaymentMethodMapper.toResponse(saved);
    }
}

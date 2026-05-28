package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeletePaymentMethodUseCase {

    private final PaymentMethodRepository repository;

    public DeletePaymentMethodUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID id) {
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);

        if (!repository.findById(paymentMethodId).isPresent()) {
            throw new IllegalArgumentException("Payment method not found: " + id);
        }

        repository.delete(paymentMethodId);
    }
}

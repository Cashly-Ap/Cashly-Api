package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;
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

        if (repository.hasPayments(paymentMethodId)) {
            throw new IllegalStateException(
                    "Cannot delete payment method with ID " + id + " because it has associated payments"
            );
        }

        repository.delete(paymentMethodId);
    }
}

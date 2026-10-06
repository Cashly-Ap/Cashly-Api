package com.cashly.cashly_api.payment_methods.application.usecases;

import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
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

        if (repository.hasAssociatedRecords(paymentMethodId)) {
            throw new IllegalStateException(
                    "No se puede eliminar el método de pago porque tiene pagos o cierres de caja asociados.");
        }

        repository.delete(paymentMethodId);
    }
}

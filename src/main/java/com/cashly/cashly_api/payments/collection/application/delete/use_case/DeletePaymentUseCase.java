package com.cashly.cashly_api.payments.collection.application.delete.use_case;

import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;

import java.util.UUID;

public class DeletePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public DeletePaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void execute(UUID id) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        paymentRepository.deleteById(PaymentId.of(id));
    }
}

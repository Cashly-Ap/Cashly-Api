package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.payments.application.dto.request.UpdatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class UpdatePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public UpdatePaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse execute(UUID id, UpdatePaymentRequest request) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        String referenceNumber = request.referenceNumber() != null ? request.referenceNumber() : payment.getReferenceNumber();
        LocalDate paymentDate = request.paymentDate() != null ? request.paymentDate() : payment.getPaymentDate();
        UUID paymentMethodId = request.paymentMethodId() != null ? request.paymentMethodId() : payment.getPaymentMethodId();
        String notes = request.notes() != null ? request.notes() : payment.getNotes();

        Payment updated = payment.withUpdatedDetails(referenceNumber, paymentDate, paymentMethodId, notes);
        Payment saved = paymentRepository.save(updated);
        return PaymentMapper.toResponse(saved);
    }
}

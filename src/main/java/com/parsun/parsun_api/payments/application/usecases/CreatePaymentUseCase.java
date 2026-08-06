package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CreatePaymentUseCase(PaymentRepository paymentRepository,
                                 CustomerRepository customerRepository,
                                 UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse execute(CreatePaymentRequest request, UUID authenticatedUserId) {
        customerRepository.findById(new CustomerId(request.customerId()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        UUID createdById = null;
        String createdByName = null;
        if (authenticatedUserId != null) {
            User user = userRepository.findById(new UserId(authenticatedUserId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));
            createdById = authenticatedUserId;
            createdByName = user.getUserName().getValue();
        }

        PaymentStatus status = PaymentStatus.valueOf(request.status());
        validateRefund(request.customerId(), status, request.amount());

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                request.customerId(),
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.notes(),
                createdById,
                createdByName,
                status
        );

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }

    private void validateRefund(UUID customerId, PaymentStatus status, BigDecimal amount) {
        if (status != PaymentStatus.REFUND) {
            return;
        }

        BigDecimal refundableAmount = paymentRepository.getRefundableAmountByCustomerId(customerId);
        if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot create a refund because the customer has no applied payments");
        }
        if (amount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed customer's refundable balance");
        }
    }
}

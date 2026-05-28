package com.parsun.parsun_api.payments.application.usecases;

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
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;
import jakarta.transaction.Transactional;

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

        String createdByName = userRepository.findById(new UserId(authenticatedUserId))
                .map(user -> user.getUserName().getValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        PaymentType type = PaymentType.valueOf(request.type());

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                request.customerId(),
                type,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.notes(),
                authenticatedUserId,
                createdByName
        );

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }
}

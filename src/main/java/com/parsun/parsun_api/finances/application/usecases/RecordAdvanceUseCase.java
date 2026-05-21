package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.finances.application.dto.request.RecordAdvanceRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.finances.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentMethodEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentMethodJpaRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RecordAdvanceUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    public RecordAdvanceUseCase(PaymentRepository paymentRepository,
                                PaymentAllocationRepository allocationRepository,
                                CustomerRepository customerRepository,
                                UserRepository userRepository,
                                PaymentMethodJpaRepository paymentMethodJpaRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.paymentMethodJpaRepository = paymentMethodJpaRepository;
    }

    @Transactional
    public PaymentDetailResponse execute(RecordAdvanceRequest request, UUID authenticatedUserId) {
        Customer customer = customerRepository.findById(new CustomerId(request.customerId()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                customer.getId().getValue(),
                PaymentType.ADVANCE,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.notes(),
                user.getId().getValue(),
                user.getUserName().getValue()
        );

        Payment saved = paymentRepository.save(payment);

        List<PaymentAllocation> allocations = allocationRepository.findByPaymentId(saved.getId().value());
        String paymentMethodName = paymentMethodJpaRepository.findById(request.paymentMethodId())
                .map(PaymentMethodEntity::getName)
                .orElse(null);

        return FinanceMapper.toPaymentDetailResponse(saved, allocations, paymentMethodName, customer.getLegalName().getValue());
    }
}

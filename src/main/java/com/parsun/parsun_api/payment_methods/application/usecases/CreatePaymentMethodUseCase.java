package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.request.CreatePaymentMethodRequest;
import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.infrastructure.persistence.repositories.PaymentMethodJpaRepository;
import jakarta.transaction.Transactional;

public class CreatePaymentMethodUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodJpaRepository jpaRepository;

    public CreatePaymentMethodUseCase(PaymentMethodRepository repository,
                                       PaymentMethodJpaRepository jpaRepository) {
        this.repository = repository;
        this.jpaRepository = jpaRepository;
    }

    @Transactional
    public PaymentMethodResponse execute(CreatePaymentMethodRequest request) {
        String code = generateNextCode();

        PaymentMethod paymentMethod = PaymentMethod.create(code, request.name(), request.description());
        PaymentMethod saved = repository.save(paymentMethod);

        return PaymentMethodMapper.toResponse(saved);
    }

    private String generateNextCode() {
        int maxNumber = jpaRepository.findMaxCodeNumber().orElse(0);
        int nextNumber = maxNumber + 1;
        return String.format("PM-%04d", nextNumber);
    }
}

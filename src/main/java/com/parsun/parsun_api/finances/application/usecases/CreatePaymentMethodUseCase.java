package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.CreatePaymentMethodRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentMethodJpaRepository;
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

        return FinanceMapper.toPaymentMethodResponse(saved);
    }

    private String generateNextCode() {
        int maxNumber = jpaRepository.findMaxCodeNumber().orElse(0);
        int nextNumber = maxNumber + 1;
        return String.format("PM-%04d", nextNumber);
    }
}

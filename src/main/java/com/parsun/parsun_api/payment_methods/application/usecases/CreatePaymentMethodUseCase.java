package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.request.CreatePaymentMethodRequest;
import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentMethodUseCase {

    private final PaymentMethodRepository repository;

    public CreatePaymentMethodUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentMethodResponse execute(CreatePaymentMethodRequest request) {
        String code = generateNextCode();

        PaymentMethod paymentMethod = PaymentMethod.create(code, request.name(), request.description());
        PaymentMethod saved = repository.save(paymentMethod);

        return PaymentMethodMapper.toResponse(saved);
    }

    private String generateNextCode() {
        int maxNumber = repository.findMaxCodeNumber().orElse(0);
        int nextNumber = maxNumber + 1;
        return String.format("PM-%04d", nextNumber);
    }
}

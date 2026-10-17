package com.cashly.cashly_api.finance.treasury.payment_methods.application.create.use_case;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.create.request.CreatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.mappers.PaymentMethodMapper;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.repository.PaymentMethodRepository;
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

        return PaymentMethodMapper.toResponse(saved, false);
    }

    private String generateNextCode() {
        int maxNumber = repository.findMaxCodeNumber().orElse(0);
        int nextNumber = maxNumber + 1;
        return String.format("PM-%04d", nextNumber);
    }
}

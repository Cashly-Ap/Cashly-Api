package com.cashly.cashly_api.payment_methods.application.usecases;

import com.cashly.cashly_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.cashly.cashly_api.payment_methods.domain.entities.PaymentMethod;
import com.cashly.cashly_api.payment_methods.application.contracts.PaymentMethodUsageGateway;
import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetPaymentMethodByIdUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodUsageGateway usageGateway;

    public GetPaymentMethodByIdUseCase(PaymentMethodRepository repository, PaymentMethodUsageGateway usageGateway) {
        this.repository = repository;
        this.usageGateway = usageGateway;
    }

    public PaymentMethodResponse execute(UUID id) {
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);
        PaymentMethod paymentMethod = repository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + id));

        boolean hasAssociatedRecords = usageGateway.hasAssociatedRecords(paymentMethodId.value());
        return PaymentMethodMapper.toResponse(paymentMethod, hasAssociatedRecords);
    }
}

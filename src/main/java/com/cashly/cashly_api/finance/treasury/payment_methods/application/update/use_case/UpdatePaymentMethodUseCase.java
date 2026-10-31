package com.cashly.cashly_api.finance.treasury.payment_methods.application.update.use_case;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.update.request.UpdatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.mappers.PaymentMethodMapper;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.repository.PaymentMethodRepository;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.valueobjects.PaymentMethodId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdatePaymentMethodUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodUsageGateway usageGateway;

    public UpdatePaymentMethodUseCase(PaymentMethodRepository repository,
            PaymentMethodUsageGateway usageGateway) {
        this.repository = repository;
        this.usageGateway = usageGateway;
    }

    @Transactional
    public PaymentMethodResponse execute(UUID id,
            UpdatePaymentMethodRequest request) {
        PaymentMethod paymentMethod = repository
                .findById(PaymentMethodId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment method not found: " + id));

        paymentMethod.update(request.name(), request.description(),
                request.isActive());
        PaymentMethod saved = repository.save(paymentMethod);

        return PaymentMethodMapper.toResponse(saved,
                usageGateway.hasAssociatedRecords(saved.getId().value()));
    }
}

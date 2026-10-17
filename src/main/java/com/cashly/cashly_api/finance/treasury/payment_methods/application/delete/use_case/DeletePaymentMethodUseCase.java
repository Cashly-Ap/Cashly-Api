package com.cashly.cashly_api.finance.treasury.payment_methods.application.delete.use_case;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.repository.PaymentMethodRepository;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.valueobjects.PaymentMethodId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeletePaymentMethodUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodUsageGateway usageGateway;

    public DeletePaymentMethodUseCase(PaymentMethodRepository repository, PaymentMethodUsageGateway usageGateway) {
        this.repository = repository;
        this.usageGateway = usageGateway;
    }

    @Transactional
    public void execute(UUID id) {
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);

        if (!repository.findById(paymentMethodId).isPresent()) {
            throw new IllegalArgumentException("Payment method not found: " + id);
        }

        if (usageGateway.hasAssociatedRecords(paymentMethodId.value())) {
            throw new IllegalStateException(
                    "No se puede eliminar el método de pago porque tiene pagos o cierres de caja asociados.");
        }

        repository.delete(paymentMethodId);
    }
}

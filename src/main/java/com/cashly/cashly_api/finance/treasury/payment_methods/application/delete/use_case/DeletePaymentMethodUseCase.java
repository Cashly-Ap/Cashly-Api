package com.cashly.cashly_api.finance.treasury.payment_methods.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

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

    private final ActivityNotificationRecorder activityNotifications;

    public DeletePaymentMethodUseCase(PaymentMethodRepository repository,
            PaymentMethodUsageGateway usageGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.repository = repository;
        this.usageGateway = usageGateway;
    }

    @Transactional
    public void execute(UUID id) {
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);

        if (!repository.findById(paymentMethodId).isPresent()) {
            throw new IllegalArgumentException(
                    "Payment method not found: " + id);
        }

        if (usageGateway.hasAssociatedRecords(paymentMethodId.value())) {
            throw new IllegalStateException(
                    "No se puede eliminar el método de pago porque tiene pagos o cierres de caja asociados.");
        }

        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_PAYMENT_METHODS",
                "DELETE", "PAYMENT_METHOD",
                paymentMethodId.value(), id.toString(), null));
        repository.delete(paymentMethodId);
    }
}

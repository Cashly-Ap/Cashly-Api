package com.cashly.cashly_api.finance.treasury.payment_methods.application.toggle.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

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
public class TogglePaymentMethodUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodUsageGateway usageGateway;

    private final ActivityNotificationRecorder activityNotifications;

    public TogglePaymentMethodUseCase(PaymentMethodRepository repository,
            PaymentMethodUsageGateway usageGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.repository = repository;
        this.usageGateway = usageGateway;
    }

    @Transactional
    public PaymentMethodResponse execute(UUID id) {
        PaymentMethod paymentMethod = repository
                .findById(PaymentMethodId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment method not found: " + id));

        paymentMethod.toggleActive();
        PaymentMethod saved = repository.save(paymentMethod);

        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_PAYMENT_METHODS",
                "EDIT", "PAYMENT_METHOD",
                saved.getId().value(), saved.getName(), null));
        return PaymentMethodMapper.toResponse(saved,
                usageGateway.hasAssociatedRecords(saved.getId().value()));
    }
}

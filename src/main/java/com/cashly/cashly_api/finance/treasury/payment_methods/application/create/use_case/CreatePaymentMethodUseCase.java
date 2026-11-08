package com.cashly.cashly_api.finance.treasury.payment_methods.application.create.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.create.request.CreatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.mappers.PaymentMethodMapper;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.repository.PaymentMethodRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class CreatePaymentMethodUseCase {

    private final PaymentMethodRepository repository;

    private final ActivityNotificationRecorder activityNotifications;

    public CreatePaymentMethodUseCase(PaymentMethodRepository repository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.repository = repository;
    }

    @Transactional
    @Audited(module = "finance", entityType = "PAYMENT_METHOD", action = "CREATE")
    public PaymentMethodResponse execute(CreatePaymentMethodRequest request) {
        String code = generateNextCode();

        PaymentMethod paymentMethod = PaymentMethod.create(code, request.name(),
                request.description());
        PaymentMethod saved = repository.save(paymentMethod);

        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_PAYMENT_METHODS",
                "CREATE", "PAYMENT_METHOD",
                saved.getId().value(), saved.getName(), null));
        return PaymentMethodMapper.toResponse(saved, false);
    }

    private String generateNextCode() {
        int maxNumber = repository.findMaxCodeNumber().orElse(0);
        int nextNumber = maxNumber + 1;
        return String.format("PM-%04d", nextNumber);
    }
}

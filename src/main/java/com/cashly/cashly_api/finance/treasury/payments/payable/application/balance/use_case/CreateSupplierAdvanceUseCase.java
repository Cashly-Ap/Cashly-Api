package com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierBalanceResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceCashGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateSupplierAdvanceUseCase {

    private final SupplierBalanceGateway balances;
    private final SupplierBalanceCashGateway cashGateway;
    private final ActivityNotificationRecorder activityNotifications;

    public CreateSupplierAdvanceUseCase(SupplierBalanceGateway balances,
            SupplierBalanceCashGateway cashGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.balances = balances;
        this.cashGateway = cashGateway;
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
    }

    @Transactional
    public SupplierBalanceResponse execute(UUID supplierId, BigDecimal amount,
            UUID paymentMethodId, String notes, UUID actorId,
            String actorName) {
        cashGateway.requireOpenRegister();
        String description = description(notes, "Anticipo a proveedor");
        SupplierBalanceGateway.BalanceMutation mutation = balances
                .registerTransaction(supplierId, "ADVANCED",
                        "SUPPLIER_BALANCE", null, description, amount, actorId);
        cashGateway.record("OUT", "EXPENSE", amount, paymentMethodId,
                description, "SUPPLIER_ADVANCE", mutation.transactionId(),
                actorId, actorName);
        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_PAYABLES",
                "ADVANCE", "SUPPLIER", supplierId, supplierId.toString(),
                actorId));
        SupplierBalanceGateway.BalanceSnapshot snapshot = mutation.balance();
        return new SupplierBalanceResponse(snapshot.supplierId(),
                snapshot.totalBalance(), snapshot.createdAt(),
                snapshot.updatedAt());
    }

    private String description(String notes, String fallback) {
        return notes == null || notes.isBlank() ? fallback : notes;
    }
}

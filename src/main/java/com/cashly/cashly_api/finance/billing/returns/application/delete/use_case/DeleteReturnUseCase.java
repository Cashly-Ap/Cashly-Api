package com.cashly.cashly_api.finance.billing.returns.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.returns.application.get.use_case.GetReturnByIdUseCase;

import com.cashly.cashly_api.finance.billing.returns.domain.entity.CancelledAllocation;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnId;
import com.cashly.cashly_api.finance.billing.returns.domain.repository.ReturnRepository;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnPaymentSettlementGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnBalanceGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInventoryGateway;
import java.math.BigDecimal;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class DeleteReturnUseCase {

    private final ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway;
    private final ReturnRepository returnRepository;
    private final ReturnInvoiceQueryGateway invoiceQueries;
    private final ReturnInventoryGateway inventoryGateway;
    private final ReturnPaymentSettlementGateway paymentSettlements;
    private final ReturnBalanceGateway balances;
    private final ActivityNotificationRecorder activityNotifications;

    public DeleteReturnUseCase(
            ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway,
            ReturnRepository returnRepository,
            ReturnInvoiceQueryGateway invoiceQueries,
            ReturnInventoryGateway inventoryGateway,
            ReturnPaymentSettlementGateway paymentSettlements,
            ReturnBalanceGateway balances,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceAdjustmentGateway = invoiceAdjustmentGateway;
        this.returnRepository = returnRepository;
        this.invoiceQueries = invoiceQueries;
        this.inventoryGateway = inventoryGateway;
        this.paymentSettlements = paymentSettlements;
        this.balances = balances;
    }

    @Transactional
    @Audited(module = "finance", entityType = "RETURN", action = "DELETE")
    public void execute(UUID id, UUID authenticatedUserId, String userName) {
        ReturnId returnId = ReturnId.of(id);
        Return return_ = returnRepository.findById(returnId).orElseThrow(
                () -> new GetReturnByIdUseCase.ReturnNotFoundException(id));

        inventoryGateway.reverseStock(
                ReturnInventoryGateway.Command.from(return_, userName));

        revertInvoiceReturn(return_);

        revertCancelledPayments(return_);

        revertCustomerBalance(return_);

        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_RETURNS",
                "DELETE", "RETURN", return_.getId().value(),
                return_.getReturnNumber().value(), authenticatedUserId));
        returnRepository.delete(returnId);
    }

    private void revertInvoiceReturn(Return return_) {
        var invoice = invoiceQueries.findById(return_.getInvoiceId())
                .orElse(null);

        if (invoice == null || invoice.cancelled()) {
            return;
        }

        BigDecimal totalPaid = paymentSettlements
                .allocatedAmount(return_.getInvoiceId());

        BigDecimal totalCreditNotes = invoiceQueries
                .creditNoteAmounts(return_.getInvoiceId()).stream()
                .map(c -> c != null ? c : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = invoiceQueries
                .debitNoteAmounts(return_.getInvoiceId()).stream()
                .map(d -> d != null ? d : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoiceAdjustmentGateway.reverse(return_, totalPaid, totalDebitNotes,
                totalCreditNotes);
    }

    private void revertCancelledPayments(Return return_) {
        paymentSettlements.restore(return_.getCancelledAllocations());
    }

    private void revertCustomerBalance(Return return_) {
        BigDecimal totalPaid = paymentSettlements
                .allocatedAmount(return_.getInvoiceId());

        BigDecimal totalCreditNotes = invoiceQueries
                .creditNoteAmounts(return_.getInvoiceId()).stream()
                .map(c -> c != null ? c : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = invoiceQueries
                .debitNoteAmounts(return_.getInvoiceId()).stream()
                .map(d -> d != null ? d : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var invoice = invoiceQueries.findById(return_.getInvoiceId())
                .orElse(null);
        if (invoice == null) {
            return;
        }

        BigDecimal effectiveTotal = invoice.calculateTotal(totalCreditNotes,
                totalDebitNotes);
        if (effectiveTotal.compareTo(BigDecimal.ZERO) < 0) {
            effectiveTotal = BigDecimal.ZERO;
        }
        BigDecimal surplus = totalPaid.subtract(effectiveTotal);

        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            balances.debit(new ReturnBalanceGateway.Command(
                    return_.getCustomerId(), surplus, return_.getId().value(),
                    "REV-" + return_.getReturnNumber().value(),
                    invoice.number(), return_.getCreatedBy()));
        }
    }
}

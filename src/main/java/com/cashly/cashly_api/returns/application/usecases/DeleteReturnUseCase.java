package com.cashly.cashly_api.returns.application.usecases;

import com.cashly.cashly_api.returns.domain.entities.CancelledAllocation;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnId;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.returns.application.contracts.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnPaymentSettlementGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnBalanceGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnInventoryGateway;
import java.math.BigDecimal;
import java.util.UUID;

public class DeleteReturnUseCase {

    private final ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway;
    private final ReturnRepository returnRepository;
    private final ReturnInvoiceQueryGateway invoiceQueries;
    private final ReturnInventoryGateway inventoryGateway;
    private final ReturnPaymentSettlementGateway paymentSettlements;
    private final ReturnBalanceGateway balances;
    public DeleteReturnUseCase(ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway, ReturnRepository returnRepository,
            ReturnInvoiceQueryGateway invoiceQueries, ReturnInventoryGateway inventoryGateway,
            ReturnPaymentSettlementGateway paymentSettlements, ReturnBalanceGateway balances) {
        this.invoiceAdjustmentGateway = invoiceAdjustmentGateway; this.returnRepository = returnRepository;
        this.invoiceQueries = invoiceQueries; this.inventoryGateway = inventoryGateway;
        this.paymentSettlements = paymentSettlements; this.balances = balances;
    }

    @Transactional
    public void execute(UUID id, UUID authenticatedUserId, String userName) {
        ReturnId returnId = ReturnId.of(id);
        Return return_ = returnRepository.findById(returnId)
                .orElseThrow(() -> new GetReturnByIdUseCase.ReturnNotFoundException(id));

        inventoryGateway.reverseStock(ReturnInventoryGateway.Command.from(return_, userName));

        revertInvoiceReturn(return_);

        revertCancelledPayments(return_);

        revertCustomerBalance(return_);

        returnRepository.delete(returnId);
    }

    private void revertInvoiceReturn(Return return_) {
        var invoice = invoiceQueries.findById(return_.getInvoiceId()).orElse(null);

        if (invoice == null || invoice.cancelled()) {
            return;
        }


        BigDecimal totalPaid = paymentSettlements.allocatedAmount(return_.getInvoiceId());

        BigDecimal totalCreditNotes = invoiceQueries.creditNoteAmounts(return_.getInvoiceId()).stream()
                .map(c -> c != null ? c : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = invoiceQueries.debitNoteAmounts(return_.getInvoiceId()).stream()
                .map(d -> d != null ? d : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoiceAdjustmentGateway.reverse(return_, totalPaid, totalDebitNotes, totalCreditNotes);
    }

    private void revertCancelledPayments(Return return_) {
        paymentSettlements.restore(return_.getCancelledAllocations());
    }

    private void revertCustomerBalance(Return return_) {
        BigDecimal totalPaid = paymentSettlements.allocatedAmount(return_.getInvoiceId());

        BigDecimal totalCreditNotes = invoiceQueries.creditNoteAmounts(return_.getInvoiceId()).stream()
                .map(c -> c != null ? c : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = invoiceQueries.debitNoteAmounts(return_.getInvoiceId()).stream()
                .map(d -> d != null ? d : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var invoice = invoiceQueries.findById(return_.getInvoiceId()).orElse(null);
        if (invoice == null) {
            return;
        }

        BigDecimal effectiveTotal = invoice.calculateTotal(totalCreditNotes, totalDebitNotes);
        if (effectiveTotal.compareTo(BigDecimal.ZERO) < 0) {
            effectiveTotal = BigDecimal.ZERO;
        }
        BigDecimal surplus = totalPaid.subtract(effectiveTotal);

        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            balances.debit(new ReturnBalanceGateway.Command(return_.getCustomerId(), surplus, return_.getId().value(),
                    "REV-" + return_.getReturnNumber().value(), invoice.number(), return_.getCreatedBy()));
        }
    }
}

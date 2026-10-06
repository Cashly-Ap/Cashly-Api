package com.cashly.cashly_api.returns.application.usecases;

import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.entities.PaymentAllocation;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.returns.domain.entities.CancelledAllocation;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnId;
import com.cashly.cashly_api.returns.domain.entities.ReturnLine;
import com.cashly.cashly_api.returns.domain.entities.ReturnLineAllocation;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class DeleteReturnUseCase {

    private final ReturnRepository returnRepository;
    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;

    public DeleteReturnUseCase(ReturnRepository returnRepository,
                                InvoiceRepository invoiceRepository,
                                WarehouseStockRepository warehouseStockRepository,
                                StockMovementRepository stockMovementRepository,
                                ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                PaymentRepository paymentRepository,
                                PaymentAllocationRepository allocationRepository,
                                CreditNoteRepository creditNoteRepository,
                                DebitNoteRepository debitNoteRepository) {
        this.returnRepository = returnRepository;
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.consumeCustomerBalanceUseCase = consumeCustomerBalanceUseCase;
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
    }

    @Transactional
    public void execute(UUID id, UUID authenticatedUserId, String userName) {
        ReturnId returnId = ReturnId.of(id);
        Return return_ = returnRepository.findById(returnId)
                .orElseThrow(() -> new GetReturnByIdUseCase.ReturnNotFoundException(id));

        revertStockFromReturn(return_, userName);

        revertInvoiceReturn(return_);

        revertCancelledPayments(return_);

        revertCustomerBalance(return_);

        returnRepository.delete(returnId);
    }

    private void revertStockFromReturn(Return return_, String userName) {
        for (ReturnLine line : return_.getLines()) {
            for (ReturnLineAllocation allocation : line.getAllocations()) {
                int quantity = allocation.getAllocated().intValue();
                if (quantity <= 0) {
                    continue;
                }

                WarehouseStock stock = warehouseStockRepository
                        .findByWarehouseIdAndProductIdForUpdate(allocation.getWarehouseId(), line.getProductId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Stock not found for warehouse=" + allocation.getWarehouseId()
                                + " product=" + line.getProductId()));

                int previousStock = stock.getQuantity();
                stock.removeStock(quantity);
                warehouseStockRepository.save(stock);

                stockMovementRepository.save(StockMovement.createWithReference(
                        allocation.getWarehouseId(),
                        line.getProductId(),
                        MovementType.CANCEL_ENTRY,
                        quantity,
                        previousStock,
                        stock.getQuantity(),
                        null,
                        null,
                        ReferenceType.RETURN,
                        return_.getId().value(),
                        return_.getReturnNumber().value(),
                        "Return deleted: " + return_.getReturnNumber().value() + " by " + userName
                ));
            }
        }
    }

    private void revertInvoiceReturn(Return return_) {
        Invoice invoice = invoiceRepository.findById(return_.getInvoiceId())
                .orElse(null);

        if (invoice == null || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            return;
        }

        invoice.revertReturn(return_);

        BigDecimal totalPaid = allocationRepository.getAllocatedAmountByInvoiceId(return_.getInvoiceId().value());

        BigDecimal totalCreditNotes = creditNoteRepository.findByInvoiceId(return_.getInvoiceId().value()).stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = debitNoteRepository.findByInvoiceId(return_.getInvoiceId().value()).stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.recalculatePaymentStatus(totalPaid, totalDebitNotes, totalCreditNotes);
        invoiceRepository.save(invoice);
    }

    private void revertCancelledPayments(Return return_) {
        for (CancelledAllocation ca : return_.getCancelledAllocations()) {
            Payment payment = paymentRepository.findById(PaymentId.of(ca.getPaymentId()))
                    .orElseThrow(() -> new IllegalStateException(
                            "Payment not found: " + ca.getPaymentId()));

            payment.reactivate();
            paymentRepository.save(payment);

            PaymentAllocation allocation = PaymentAllocation.create(
                    ca.getPaymentId(), ca.getInvoiceId(), ca.getAmount());
            allocationRepository.save(allocation);
        }
    }

    private void revertCustomerBalance(Return return_) {
        BigDecimal totalPaid = allocationRepository.getAllocatedAmountByInvoiceId(return_.getInvoiceId().value());

        BigDecimal totalCreditNotes = creditNoteRepository.findByInvoiceId(return_.getInvoiceId().value()).stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = debitNoteRepository.findByInvoiceId(return_.getInvoiceId().value()).stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice invoice = invoiceRepository.findById(return_.getInvoiceId()).orElse(null);
        if (invoice == null) {
            return;
        }

        BigDecimal effectiveTotal = invoice.calculateTotal(totalCreditNotes, totalDebitNotes);
        if (effectiveTotal.compareTo(BigDecimal.ZERO) < 0) {
            effectiveTotal = BigDecimal.ZERO;
        }
        BigDecimal surplus = totalPaid.subtract(effectiveTotal);

        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            consumeCustomerBalanceUseCase.execute(
                    return_.getCustomerId(),
                    BalanceType.RETURNS,
                    surplus,
                    BalanceType.RETURNS,
                    return_.getId().value(),
                    "REV-" + return_.getReturnNumber().value(),
                    invoice.getInvoiceNumber().value(),
                    return_.getCreatedBy()
            );
        }
    }
}

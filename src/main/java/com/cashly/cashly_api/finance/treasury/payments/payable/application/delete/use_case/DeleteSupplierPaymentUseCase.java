package com.cashly.cashly_api.finance.treasury.payments.payable.application.delete.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentId;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteSupplierPaymentUseCase {

    private final SupplierPaymentRepository payments;

    public DeleteSupplierPaymentUseCase(SupplierPaymentRepository payments) {
        this.payments = payments;
    }

    @Transactional
    public void execute(UUID id) {
        SupplierPayment payment = payments
                .findById(SupplierPaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier payment not found: " + id));
        if (payment.getStatus() == SupplierPaymentStatus.APPLIED) {
            throw new IllegalStateException(
                    "Cannot delete an applied supplier payment");
        }
        payments.deleteById(SupplierPaymentId.of(id));
    }
}

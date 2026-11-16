package com.cashly.cashly_api.finance.treasury.payments.payable.application.approve.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierPaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.mappers.SupplierPaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.services.SupplierPaymentSettlementService;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentId;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ApproveSupplierPaymentUseCase {

    private final SupplierPaymentRepository payments;
    private final SupplierPaymentSettlementService settlement;

    public ApproveSupplierPaymentUseCase(SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        this.payments = payments;
        this.settlement = settlement;
    }

    @Transactional
    public SupplierPaymentResponse execute(UUID id, String reason,
            UUID actorId, String actorName) {
        SupplierPayment payment = payments
                .findById(SupplierPaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier payment not found: " + id));
        payment.approve(reason, actorId, actorName);
        SupplierPayment saved = payments.save(payment);
        saved = settlement.settle(saved, actorId, actorName);
        return SupplierPaymentMapper.toResponse(saved);
    }
}

package com.cashly.cashly_api.finance.treasury.payments.payable.application.pay_purchase.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.PayPurchaseRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierPaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.mappers.SupplierPaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.PayablePurchaseGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.services.SupplierPaymentSettlementService;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class PayPurchaseUseCase {

    private final PayablePurchaseGateway purchaseGateway;
    private final SupplierPaymentRepository payments;
    private final SupplierPaymentSettlementService settlement;

    public PayPurchaseUseCase(PayablePurchaseGateway purchaseGateway,
            SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        this.purchaseGateway = purchaseGateway;
        this.payments = payments;
        this.settlement = settlement;
    }

    @Transactional
    public SupplierPaymentResponse execute(UUID purchaseId,
            PayPurchaseRequest request, UUID actorId, String actorName) {
        PayablePurchaseGateway.PayablePurchase purchase = purchaseGateway
                .findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + purchaseId));
        int year = LocalDate.now().getYear();
        SupplierPaymentNumber number = SupplierPaymentNumber.of(year,
                payments.getNextSequenceForYear(year));
        SupplierPayment payment = SupplierPayment.create(number,
                purchase.supplierId(), request.amount(),
                request.paymentMethodId(), request.referenceNumber(),
                request.paymentDate() != null
                        ? request.paymentDate()
                        : LocalDate.now(),
                request.notes(), actorId, actorName,
                SupplierPaymentStatus.APPLIED);
        SupplierPayment saved = payments.save(payment);
        saved = settlement.settlePurchase(saved, purchaseId, request.amount(),
                actorId, actorName);
        return SupplierPaymentMapper.toResponse(saved);
    }
}

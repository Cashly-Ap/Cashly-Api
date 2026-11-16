package com.cashly.cashly_api.finance.treasury.payments.payable.application.pay_supplier_debt.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.PaySupplierDebtRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierPaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.mappers.SupplierPaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.services.SupplierPaymentSettlementService;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class PaySupplierDebtUseCase {

    private final SupplierPaymentRepository payments;
    private final SupplierPaymentSettlementService settlement;

    public PaySupplierDebtUseCase(SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        this.payments = payments;
        this.settlement = settlement;
    }

    @Transactional
    public SupplierPaymentResponse execute(UUID supplierId,
            PaySupplierDebtRequest request, UUID actorId, String actorName) {
        int year = LocalDate.now().getYear();
        SupplierPaymentNumber number = SupplierPaymentNumber.of(year,
                payments.getNextSequenceForYear(year));
        SupplierPayment payment = SupplierPayment.create(number, supplierId,
                request.amount(), request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate() != null
                        ? request.paymentDate()
                        : LocalDate.now(),
                request.notes(), actorId, actorName,
                SupplierPaymentStatus.APPLIED);
        SupplierPayment saved = payments.save(payment);
        saved = settlement.settle(saved, actorId, actorName);
        return SupplierPaymentMapper.toResponse(saved);
    }
}

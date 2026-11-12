package com.cashly.cashly_api.finance.treasury.payments.payable.application.create.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.CreateSupplierPaymentRequest;
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

public class RegisterSupplierPaymentUseCase {

    private final SupplierPaymentRepository payments;
    private final SupplierPaymentSettlementService settlement;

    public RegisterSupplierPaymentUseCase(SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        this.payments = payments;
        this.settlement = settlement;
    }

    @Transactional
    public SupplierPaymentResponse execute(CreateSupplierPaymentRequest request,
            UUID actorId, String actorName) {
        int year = LocalDate.now().getYear();
        SupplierPaymentNumber number = SupplierPaymentNumber.of(year,
                payments.getNextSequenceForYear(year));
        SupplierPaymentStatus status = request.status() == null
                ? SupplierPaymentStatus.PENDING
                : SupplierPaymentStatus.valueOf(request.status().toUpperCase());
        SupplierPayment payment = SupplierPayment.create(number,
                request.supplierId(), request.amount(),
                request.paymentMethodId(), request.referenceNumber(),
                request.paymentDate() != null
                        ? request.paymentDate()
                        : LocalDate.now(),
                request.notes(), actorId, actorName, status);
        SupplierPayment saved = payments.save(payment);
        if (status == SupplierPaymentStatus.APPLIED) {
            saved = settlement.settle(saved, actorId, actorName);
        }
        return SupplierPaymentMapper.toResponse(saved);
    }
}

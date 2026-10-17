package com.cashly.cashly_api.finance.treasury.payments.collection.application.create.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentActorGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.create.request.CreatePaymentRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentCustomerGateway customers;
    private final PaymentActorGateway actors;
    private final CashRegisterGateway cashRegister;

    public CreatePaymentUseCase(PaymentRepository paymentRepository,
                                 PaymentCustomerGateway customers,
                                 PaymentActorGateway actors,
                                 CashRegisterGateway cashRegister) {
        this.paymentRepository = paymentRepository;
        this.customers = customers;
        this.actors = actors;
        this.cashRegister = cashRegister;
    }

    @Transactional
    public PaymentResponse execute(CreatePaymentRequest request, UUID authenticatedUserId) {
        if (!customers.exists(request.customerId())) {
            throw new IllegalArgumentException("Customer not found: " + request.customerId());
        }

        UUID createdById = null;
        String createdByName = null;
        if (authenticatedUserId != null) {
            String userName = actors.findName(authenticatedUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));
            createdById = authenticatedUserId;
            createdByName = userName;
        }

        PaymentStatus status = PaymentStatus.valueOf(request.status());
        validateRefund(request.customerId(), status, request.amount());
        if (affectsCash(status)) {
            cashRegister.requireOpenRegister();
        }

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                request.customerId(),
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.notes(),
                createdById,
                createdByName,
                status
        );

        Payment saved = paymentRepository.save(payment);
        recordCashMovement(saved);
        return PaymentMapper.toResponse(saved);
    }

    private boolean affectsCash(PaymentStatus status) {
        return status == PaymentStatus.APPLIED || status == PaymentStatus.REFUND;
    }

    private void recordCashMovement(Payment payment) {
        if (!affectsCash(payment.getStatus())) {
            return;
        }
        boolean refund = payment.getStatus() == PaymentStatus.REFUND;
        cashRegister.recordMovement(new CashRegisterGateway.Movement(
                refund ? CashRegisterGateway.Direction.OUT : CashRegisterGateway.Direction.IN,
                refund ? CashRegisterGateway.Type.REFUND : CashRegisterGateway.Type.PAYMENT,
                new CashRegisterGateway.PaymentEntry(payment.getAmount(), payment.getPaymentMethodId(),
                        payment.getNotes(), payment.getId().value(), payment.getCreatedBy(), payment.getCreatedByName())));
    }

    private void validateRefund(UUID customerId, PaymentStatus status, BigDecimal amount) {
        if (status != PaymentStatus.REFUND) {
            return;
        }

        BigDecimal refundableAmount = paymentRepository.getRefundableAmountByCustomerId(customerId);
        if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot create a refund because the customer has no applied payments");
        }
        if (amount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed customer's refundable balance");
        }
    }
}

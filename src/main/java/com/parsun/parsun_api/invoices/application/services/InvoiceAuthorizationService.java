package com.parsun.parsun_api.invoices.application.services;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.shared.services.CreditGuard;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class InvoiceAuthorizationService {

    private final CreditGuard creditGuard;
    private final PasswordEncoder passwordEncoder;
    private final ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;

    public InvoiceAuthorizationService(CreditGuard creditGuard,
                                        PasswordEncoder passwordEncoder,
                                        ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                        PaymentRepository paymentRepository,
                                        PaymentAllocationRepository allocationRepository) {
        this.creditGuard = creditGuard;
        this.passwordEncoder = passwordEncoder;
        this.consumeCustomerBalanceUseCase = consumeCustomerBalanceUseCase;
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public void authorize(Invoice invoice, User user, String confirmationPassword,
                           Boolean useBalance, BigDecimal balanceAmount,
                           UUID paymentMethodId, UUID authenticatedUserId) {
        if (invoice.getPaymentType() == PaymentType.CREDIT) {
            authorizeCredit(invoice, user, confirmationPassword);
        } else if (invoice.getPaymentType() == PaymentType.CASH
                && useBalance != null && useBalance) {
            authorizeCash(invoice, balanceAmount, paymentMethodId, authenticatedUserId, user);
        }
    }

    private void authorizeCredit(Invoice invoice, User user, String confirmationPassword) {
        boolean exceedsLimit = !creditGuard.isCreditAvailable(invoice.getCustomerId(), invoice.calculateTotal());
        if (exceedsLimit) {
            if (confirmationPassword == null || confirmationPassword.isBlank()) {
                throw new IllegalArgumentException(
                        "Se requiere contrasena para confirmar facturas que exceden el limite de credito");
            }
            if (!passwordEncoder.matches(confirmationPassword, user.getPassword().getHashedValue())) {
                throw new IllegalArgumentException("Contrasena incorrecta");
            }
        }
    }

    private void authorizeCash(Invoice invoice, BigDecimal balanceAmount,
                                UUID paymentMethodId, UUID authenticatedUserId, User user) {
        BigDecimal balanceToUse = balanceAmount != null ? balanceAmount : invoice.calculateTotal();
        if (balanceToUse.compareTo(BigDecimal.ZERO) > 0) {
            consumeCustomerBalanceUseCase.execute(
                    invoice.getCustomerId(),
                    BalanceType.INVOICES,
                    balanceToUse,
                    BalanceType.INVOICES,
                    invoice.getId().value(),
                    invoice.getInvoiceNumber().value(),
                    null,
                    authenticatedUserId
            );
        }

        BigDecimal remaining = invoice.calculateTotal().subtract(balanceToUse);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            if (paymentMethodId == null) {
                throw new IllegalArgumentException("Debe seleccionar un metodo de pago para el monto restante");
            }
            int year = LocalDate.now().getYear();
            int seq = paymentRepository.getNextSequenceForYear(year);
            Payment payment = Payment.create(
                    PaymentNumber.of(year, seq),
                    invoice.getCustomerId(),
                    remaining,
                    paymentMethodId,
                    null,
                    LocalDate.now(),
                    "Pago de factura " + invoice.getInvoiceNumber().value(),
                    authenticatedUserId,
                    user.getUserName().getValue(),
                    PaymentStatus.APPLIED
            );
            Payment savedPayment = paymentRepository.save(payment);
            PaymentAllocation allocation = PaymentAllocation.create(
                    savedPayment.getId().value(), invoice.getId().value(), remaining
            );
            allocationRepository.save(allocation);
            payment.apply(remaining);
            paymentRepository.save(payment);
        }
    }
}

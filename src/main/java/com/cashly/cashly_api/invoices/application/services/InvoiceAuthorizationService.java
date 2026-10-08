package com.cashly.cashly_api.invoices.application.services;

import com.cashly.cashly_api.invoices.application.contracts.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.contracts.CashRegisterGateway;
import com.cashly.cashly_api.invoices.application.contracts.CustomerBalanceGateway;
import com.cashly.cashly_api.invoices.application.contracts.CustomerCreditGateway;
import com.cashly.cashly_api.invoices.application.contracts.InvoicePaymentSettlementGateway;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class InvoiceAuthorizationService {
    private final CustomerCreditGateway creditGuard;
    private final ActorIdentityGateway actors;
    private final CustomerBalanceGateway balances;
    private final InvoicePaymentSettlementGateway settlements;
    private final CashRegisterGateway cashRegister;
    public InvoiceAuthorizationService(CustomerCreditGateway creditGuard, ActorIdentityGateway actors,
            CustomerBalanceGateway balances, InvoicePaymentSettlementGateway settlements, CashRegisterGateway cashRegister) {
        this.creditGuard = creditGuard;
        this.actors = actors;
        this.balances = balances;
        this.settlements = settlements;
        this.cashRegister = cashRegister;
    }
    @Transactional
    public void authorize(Invoice invoice, ActorIdentityGateway.Actor actor, String confirmationPassword,
            Boolean useBalance, BigDecimal balanceAmount, UUID paymentMethodId, UUID authenticatedUserId) {
        if (invoice.getPaymentType() == PaymentType.CREDIT) {
            authorizeCredit(invoice, actor, confirmationPassword);
        } else if (invoice.getPaymentType() == PaymentType.CASH) {
            authorizeCash(invoice, Boolean.TRUE.equals(useBalance), balanceAmount, paymentMethodId, authenticatedUserId, actor);
        }
    }
    private void authorizeCredit(Invoice invoice, ActorIdentityGateway.Actor actor, String confirmationPassword) {
        boolean exceedsLimit = !creditGuard.isCreditAvailable(invoice.getCustomerId(), invoice.calculateTotal());
        if (exceedsLimit) {
            if (confirmationPassword == null || confirmationPassword.isBlank()) {
                throw new IllegalArgumentException("Se requiere contrasena para confirmar facturas que exceden el limite de credito");
            }
            if (!actors.matchesPassword(actor.id(), confirmationPassword)) {
                throw new IllegalArgumentException("Contrasena incorrecta");
            }
        }
    }
    private void authorizeCash(Invoice invoice, boolean useBalance, BigDecimal balanceAmount,
            UUID paymentMethodId, UUID authenticatedUserId, ActorIdentityGateway.Actor actor) {
        BigDecimal balanceToUse = useBalance ? (balanceAmount != null ? balanceAmount : invoice.calculateTotal()) : BigDecimal.ZERO;
        BigDecimal remaining = invoice.calculateTotal().subtract(balanceToUse);
        if (remaining.compareTo(BigDecimal.ZERO) > 0 && paymentMethodId == null) {
            throw new IllegalArgumentException("Debe seleccionar un metodo de pago para el monto restante");
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            cashRegister.requireOpenRegister();
        }
        if (balanceToUse.compareTo(BigDecimal.ZERO) > 0) {
            balances.consumeInvoiceBalance(new CustomerBalanceGateway.Consumption(invoice.getCustomerId(), balanceToUse,
                    invoice.getId().value(), invoice.getInvoiceNumber().value(), authenticatedUserId));
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            settlements.settleCashInvoice(new InvoicePaymentSettlementGateway.Settlement(invoice.getCustomerId(), invoice.getId().value(),
                    invoice.getInvoiceNumber().value(), remaining, paymentMethodId, authenticatedUserId, actor.name()));
        }
    }
}

package com.cashly.cashly_api.finance.billing.invoices.application.issue.validation;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditGateway;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class InvoiceIssueValidator {
    private final CustomerCreditGateway creditGuard;
    private final ActorIdentityGateway actors;
    private final CashRegisterGateway cashRegister;
    public InvoiceIssueValidator(CustomerCreditGateway creditGuard,
            ActorIdentityGateway actors, CashRegisterGateway cashRegister) {
        this.creditGuard = creditGuard;
        this.actors = actors;
        this.cashRegister = cashRegister;
    }
    public CashPaymentPlan validate(Invoice invoice,
            ActorIdentityGateway.Actor actor, String confirmationPassword,
            Boolean useBalance, BigDecimal balanceAmount, UUID paymentMethodId,
            UUID authenticatedUserId) {
        if (invoice.getPaymentType() == PaymentType.CREDIT) {
            authorizeCredit(invoice, actor, confirmationPassword);
        } else if (invoice.getPaymentType() == PaymentType.CASH) {
            return validateCash(invoice, Boolean.TRUE.equals(useBalance),
                    balanceAmount, paymentMethodId);
        }
        return new CashPaymentPlan(BigDecimal.ZERO, BigDecimal.ZERO);
    }
    private void authorizeCredit(Invoice invoice,
            ActorIdentityGateway.Actor actor, String confirmationPassword) {
        boolean exceedsLimit = !creditGuard.isCreditAvailable(
                invoice.getCustomerId(), invoice.calculateTotal());
        if (exceedsLimit) {
            if (confirmationPassword == null
                    || confirmationPassword.isBlank()) {
                throw new IllegalArgumentException(
                        "Se requiere contrasena para confirmar facturas que exceden el limite de credito");
            }
            if (!actors.matchesPassword(actor.id(), confirmationPassword)) {
                throw new IllegalArgumentException("Contrasena incorrecta");
            }
        }
    }
    private CashPaymentPlan validateCash(Invoice invoice, boolean useBalance,
            BigDecimal balanceAmount, UUID paymentMethodId) {
        BigDecimal balanceToUse = useBalance
                ? (balanceAmount != null
                        ? balanceAmount
                        : invoice.calculateTotal())
                : BigDecimal.ZERO;
        BigDecimal remaining = invoice.calculateTotal().subtract(balanceToUse);
        if (remaining.compareTo(BigDecimal.ZERO) > 0
                && paymentMethodId == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un metodo de pago para el monto restante");
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            cashRegister.requireOpenRegister();
        }
        return new CashPaymentPlan(balanceToUse, remaining);
    }
    public record CashPaymentPlan(BigDecimal balanceAmount,
            BigDecimal remainingAmount) {
    }
}

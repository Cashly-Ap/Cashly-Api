package com.cashly.cashly_api.invoices.application.issue.services;

import com.cashly.cashly_api.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.invoices.application.issue.request.IssueInvoiceCommand;

import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerBalanceGateway;
import com.cashly.cashly_api.invoices.application.ports.out.InvoicePaymentSettlementGateway;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentService {
    private final CustomerBalanceGateway balances;
    private final InvoicePaymentSettlementGateway settlements;
    public InvoicePaymentService(CustomerBalanceGateway balances, InvoicePaymentSettlementGateway settlements) {
        this.balances = balances;
        this.settlements = settlements;
    }
    public void apply(Invoice invoice, InvoiceIssueValidator.CashPaymentPlan plan,
                      IssueInvoiceCommand command, ActorIdentityGateway.Actor actor) {
        if (plan.balanceAmount().signum() > 0) {
            balances.consumeInvoiceBalance(new CustomerBalanceGateway.Consumption(invoice.getCustomerId(),
                    plan.balanceAmount(), invoice.getId().value(), invoice.getInvoiceNumber().value(), actor.id()));
        }
        if (plan.remainingAmount().signum() > 0) {
            settlements.settleCashInvoice(new InvoicePaymentSettlementGateway.Settlement(invoice.getCustomerId(),
                    invoice.getId().value(), invoice.getInvoiceNumber().value(), plan.remainingAmount(),
                    command.paymentMethodId(), actor.id(), actor.name()));
        }
    }
}

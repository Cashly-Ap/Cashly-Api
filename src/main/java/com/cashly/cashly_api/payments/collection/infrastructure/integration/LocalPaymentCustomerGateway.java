package com.cashly.cashly_api.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.customers.profile.application.ports.in.InvoiceCustomerQuery;
import com.cashly.cashly_api.customers.balance.application.consume.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentCustomerGateway;
import org.springframework.stereotype.Component;

@Component
public class LocalPaymentCustomerGateway implements PaymentCustomerGateway {
    private final InvoiceCustomerQuery customers;
    private final ConsumeCustomerBalanceUseCase balances;

    public LocalPaymentCustomerGateway(InvoiceCustomerQuery customers, ConsumeCustomerBalanceUseCase balances) {
        this.customers = customers;
        this.balances = balances;
    }

    @Override
    public boolean exists(java.util.UUID customerId) {
        return customers.findCustomer(customerId).isPresent();
    }

    @Override
    public void consumeInvoiceBalance(BalanceConsumption consumption) {
        balances.execute(consumption.customerId(), BalanceType.INVOICES, consumption.amount(),
                BalanceType.INVOICES, consumption.invoiceId(), consumption.reference(), null,
                consumption.actorId());
    }
}

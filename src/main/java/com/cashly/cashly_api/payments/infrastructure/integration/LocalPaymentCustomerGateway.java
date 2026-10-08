package com.cashly.cashly_api.payments.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.InvoiceCustomerQuery;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.payments.application.contracts.PaymentCustomerGateway;
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

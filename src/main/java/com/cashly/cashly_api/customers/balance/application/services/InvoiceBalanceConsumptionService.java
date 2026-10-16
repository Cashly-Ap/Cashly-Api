package com.cashly.cashly_api.customers.balance.application.services;
import com.cashly.cashly_api.customers.balance.application.ports.in.InvoiceBalanceConsumption;
import com.cashly.cashly_api.customers.balance.application.consume.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;
public class InvoiceBalanceConsumptionService implements InvoiceBalanceConsumption {
    private final ConsumeCustomerBalanceUseCase consumeBalance;
    public InvoiceBalanceConsumptionService(ConsumeCustomerBalanceUseCase consumeBalance) { this.consumeBalance = consumeBalance; }
    @Transactional
    public void consume(Command command) {
        consumeBalance.execute(command.customerId(), BalanceType.INVOICES, command.amount(), BalanceType.INVOICES,
                command.invoiceId(), command.invoiceNumber(), null, command.actorId());
    }
}

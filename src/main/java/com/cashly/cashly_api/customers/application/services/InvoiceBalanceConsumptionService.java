package com.cashly.cashly_api.customers.application.services;
import com.cashly.cashly_api.customers.application.api.InvoiceBalanceConsumption;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.domain.valueobjects.BalanceType;
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

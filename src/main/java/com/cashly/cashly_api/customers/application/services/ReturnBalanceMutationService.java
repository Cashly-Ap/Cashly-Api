package com.cashly.cashly_api.customers.application.services;
import com.cashly.cashly_api.customers.application.api.ReturnBalanceMutation;
import com.cashly.cashly_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;
public class ReturnBalanceMutationService implements ReturnBalanceMutation {
    private final AddToCustomerBalanceUseCase add;
    private final ConsumeCustomerBalanceUseCase consume;
    public ReturnBalanceMutationService(AddToCustomerBalanceUseCase add, ConsumeCustomerBalanceUseCase consume) { this.add = add; this.consume = consume; }
    @Transactional
    public void credit(Command command) {
        add.execute(command.customerId(), BalanceType.RETURNS, command.amount(), BalanceType.RETURNS, command.returnId(),
                command.returnNumber(), command.invoiceNumber(), command.actorId());
    }
    @Transactional
    public void debit(Command command) {
        consume.execute(command.customerId(), BalanceType.RETURNS, command.amount(), BalanceType.RETURNS, command.returnId(),
                command.returnNumber(), command.invoiceNumber(), command.actorId());
    }
}

package com.cashly.cashly_api.partners.customers.balance.application.services;
import com.cashly.cashly_api.partners.customers.balance.application.ports.in.ReturnBalanceMutation;
import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
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

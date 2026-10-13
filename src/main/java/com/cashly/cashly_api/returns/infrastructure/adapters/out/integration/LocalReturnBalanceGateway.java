package com.cashly.cashly_api.returns.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.returns.application.ports.out.ReturnBalanceGateway;
import com.cashly.cashly_api.customers.application.ports.in.ReturnBalanceMutation;
import org.springframework.stereotype.Component;
@Component
public class LocalReturnBalanceGateway implements ReturnBalanceGateway {
    private final ReturnBalanceMutation mutation;
    public LocalReturnBalanceGateway(ReturnBalanceMutation mutation) { this.mutation = mutation; }
    public void credit(Command command) { mutation.credit(map(command)); }
    public void debit(Command command) { mutation.debit(map(command)); }
    private ReturnBalanceMutation.Command map(Command command) {
        return new ReturnBalanceMutation.Command(command.customerId(), command.amount(), command.returnId(), command.returnNumber(), command.invoiceNumber(), command.actorId());
    }
}

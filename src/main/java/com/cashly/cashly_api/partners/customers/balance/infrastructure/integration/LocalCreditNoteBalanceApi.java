package com.cashly.cashly_api.partners.customers.balance.infrastructure.integration;

import com.cashly.cashly_api.partners.customers.balance.application.ports.in.CreditNoteBalanceApi;
import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalCreditNoteBalanceApi implements CreditNoteBalanceApi {
    private final AddToCustomerBalanceUseCase balances;

    public LocalCreditNoteBalanceApi(AddToCustomerBalanceUseCase balances) {
        this.balances = balances;
    }

    @Override
    public void addCredit(UUID customerId, BigDecimal amount, UUID noteId,
            String noteNumber, String invoiceNumber, UUID authenticatedUserId) {
        balances.execute(customerId, BalanceType.CREDIT_NOTE, amount,
                BalanceType.CREDIT_NOTE, noteId, noteNumber, invoiceNumber,
                authenticatedUserId);
    }
}

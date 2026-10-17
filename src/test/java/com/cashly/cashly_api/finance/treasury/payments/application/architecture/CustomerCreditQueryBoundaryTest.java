package com.cashly.cashly_api.finance.treasury.payments.application.architecture;

import com.cashly.cashly_api.finance.treasury.payments.credit.application.services.CustomerCreditQueryService;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerCreditQueryBoundaryTest {
    @Test
    void should_depend_on_payments_owned_credit_gateways() {
        var dependencies = Arrays.stream(CustomerCreditQueryService.class.getConstructors()[0].getParameterTypes())
                .map(Class::getName).toList();
        assertTrue(dependencies.stream().noneMatch(type -> type.startsWith("com.cashly.cashly_api.partners.customers.")
                || type.startsWith("com.cashly.cashly_api.finance.billing.invoices.")
                || type.startsWith("com.cashly.cashly_api.finance.billing.credit_notes.")
                || type.startsWith("com.cashly.cashly_api.finance.billing.debit_notes.")), dependencies.toString());
        for (String contract : new String[]{"CustomerCreditContextGateway", "InvoiceCreditLedgerGateway", "CreditNoteLedgerGateway", "DebitNoteLedgerGateway"}) {
            assertTrue(dependencies.contains("com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out." + contract));
        }
    }
    @Test
    void should_expose_payments_owned_public_query_and_snapshots() {
        assertTrue(Arrays.stream(CustomerCreditQueryService.class.getInterfaces())
                .anyMatch(type -> type.getName().equals("com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.CustomerCreditQuery")));
        assertTrue(Arrays.stream(CustomerCreditQueryService.class.getDeclaredMethods())
                .noneMatch(method -> method.getReturnType().getName().startsWith("com.cashly.cashly_api.partners.customers.")));
    }
}

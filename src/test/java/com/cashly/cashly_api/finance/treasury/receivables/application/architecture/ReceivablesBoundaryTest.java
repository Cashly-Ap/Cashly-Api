package com.cashly.cashly_api.finance.treasury.receivables.application.architecture;

import com.cashly.cashly_api.finance.treasury.receivables.application.customer_invoices.use_case.GetCustomerInvoicesUseCase;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ReceivablesBoundaryTest {
    @Test
    void should_define_query_contract_in_application() {
        Class<?> query = GetCustomerInvoicesUseCase.class.getConstructors()[0].getParameterTypes()[0];
        assertEquals("com.cashly.cashly_api.finance.treasury.receivables.application.ports.out.ReceivablesQueryGateway", query.getName());
    }

    @Test
    void should_expose_neutral_pagination_at_query_boundary() throws Exception {
        Class<?> query = GetCustomerInvoicesUseCase.class.getConstructors()[0].getParameterTypes()[0];
        assertEquals("com.cashly.cashly_api.shared.dto.PageResponse",
                query.getMethod("findCustomerInvoices", UUID.class, String.class, String.class,
                        int.class, int.class).getReturnType().getName());
    }
}

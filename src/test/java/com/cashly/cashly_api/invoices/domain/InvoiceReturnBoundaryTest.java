package com.cashly.cashly_api.invoices.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class InvoiceReturnBoundaryTest {
    @Test
    void shouldNotExposeReturnsModuleTypesInInvoiceDomain() {
        boolean foreignReturnParameter = Arrays.stream(Invoice.class.getDeclaredMethods())
                .flatMap(method -> Arrays.stream(method.getParameterTypes()))
                .anyMatch(type -> type.getPackageName().startsWith("com.cashly.cashly_api.returns"));

        assertFalse(foreignReturnParameter);
    }
}

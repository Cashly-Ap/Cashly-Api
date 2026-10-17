package com.cashly.cashly_api.finance.billing.returns.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnAdjustments;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnCommand;

import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.ReturnLine;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InvoiceReturnAdjustmentAdapterTest {
    @Test
    void shouldTranslateAutomaticReturnToPublicInvoiceCommand() {
        Return returned = returned(ReturnType.AUTOMATIC);
        InvoiceReturnAdjustments provider = mock(InvoiceReturnAdjustments.class);
        new InvoiceReturnAdjustmentAdapter(provider).apply(returned);
        ArgumentCaptor<InvoiceReturnCommand> command = ArgumentCaptor.forClass(InvoiceReturnCommand.class);
        verify(provider).apply(command.capture());
        assertEquals(returned.getInvoiceId(), command.getValue().invoiceId());
        assertEquals(returned.getTotal(), command.getValue().total());
        assertEquals(returned.getLines().getFirst().getInvoiceLineId(), command.getValue().lines().getFirst().invoiceLineId());
        assertEquals(returned.getLines().getFirst().getQuantity(), command.getValue().lines().getFirst().quantity());
        assertTrue(command.getValue().closeInvoice());
    }

    @Test
    void shouldTranslateManualReversalAndPreserveFinancialTotals() {
        Return returned = returned(ReturnType.MANUAL);
        InvoiceReturnAdjustments provider = mock(InvoiceReturnAdjustments.class);
        new InvoiceReturnAdjustmentAdapter(provider).reverse(returned, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO);
        ArgumentCaptor<InvoiceReturnCommand> command = ArgumentCaptor.forClass(InvoiceReturnCommand.class);
        verify(provider).reverse(command.capture(), eq(BigDecimal.TEN), eq(BigDecimal.ONE), eq(BigDecimal.ZERO));
        assertFalse(command.getValue().closeInvoice());
        assertEquals(returned.getInvoiceId(), command.getValue().invoiceId());
    }

    private Return returned(ReturnType type) {
        Return returned = mock(Return.class);
        ReturnLine line = mock(ReturnLine.class);
        when(returned.getInvoiceId()).thenReturn(UUID.randomUUID());
        when(returned.getTotal()).thenReturn(BigDecimal.TEN);
        when(returned.getReturnType()).thenReturn(type);
        when(returned.getLines()).thenReturn(List.of(line));
        when(line.getInvoiceLineId()).thenReturn(UUID.randomUUID());
        when(line.getQuantity()).thenReturn(BigDecimal.ONE);
        return returned;
    }
}

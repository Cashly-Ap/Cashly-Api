package com.cashly.cashly_api.orders.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.create.from_order.services.OrderInvoiceCreationService;
import com.cashly.cashly_api.finance.billing.invoices.application.create.use_case.CreateInvoiceUseCase;
import com.cashly.cashly_api.orders.application.ports.out.InvoiceCreationGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InvoiceCreationContractTest {
    @Test
    void should_preserve_every_conversion_option_and_invoice_identity() {
        var createInvoice = mock(CreateInvoiceUseCase.class);
        var response = mock(InvoiceDetailResponse.class);
        UUID orderId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        LocalDate issueDate = LocalDate.of(2026, 10, 6);
        var balance = new BigDecimal("12.34");
        when(response.id()).thenReturn(invoiceId);
        when(response.invoiceNumber()).thenReturn("INV-2026-00001");
        when(createInvoice.execute(any(), eq(actorId))).thenReturn(response);
        var gateway = new LocalInvoiceCreationGateway(new OrderInvoiceCreationService(createInvoice));

        var invoice = gateway.create(new InvoiceCreationGateway.Command(orderId, "CASH", true,
                balance, paymentMethodId, "password", issueDate), actorId);

        var captor = ArgumentCaptor.forClass(CreateInvoiceRequest.class);
        verify(createInvoice).execute(captor.capture(), eq(actorId));
        var request = captor.getValue();
        assertThat(request.orderId()).isEqualTo(orderId);
        assertThat(request.paymentType()).isEqualTo("CASH");
        assertThat(request.useBalance()).isTrue();
        assertThat(request.balanceAmount()).isEqualTo(balance);
        assertThat(request.paymentMethodId()).isEqualTo(paymentMethodId);
        assertThat(request.confirmationPassword()).isEqualTo("password");
        assertThat(request.issueDate()).isEqualTo(issueDate);
        assertThat(invoice.id()).isEqualTo(invoiceId);
        assertThat(invoice.invoiceNumber()).isEqualTo("INV-2026-00001");
    }
}

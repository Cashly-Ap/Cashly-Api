package com.cashly.cashly_api.invoices.application.cancel.use_case;

import com.cashly.cashly_api.invoices.application.cancel.request.CancelInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.invoices.application.ports.out.InvoiceNotificationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.cashly.cashly_api.invoices.application.ports.out.InventoryGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelInvoiceUseCase Unit Tests")
class CancelInvoiceUseCaseUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InventoryGateway inventoryGateway;

    @Mock
    private InvoiceNotificationGateway notificationService;

    private CancelInvoiceUseCase cancelInvoiceUseCase;

    @BeforeEach
    void setUp() {
        cancelInvoiceUseCase = new CancelInvoiceUseCase(invoiceRepository,
                inventoryGateway, notificationService);
    }

    @Test
    void should_CancelInvoice_When_InvoiceExistsAndReasonProvided() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT);
        CancelInvoiceRequest request = new CancelInvoiceRequest("Duplicate invoice");

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        InvoiceDetailResponse response = cancelInvoiceUseCase.execute(id, request);

        assertNotNull(response);
        assertEquals("CANCELLED", response.status());
        verify(invoiceRepository).findById(InvoiceId.of(id));
        verify(invoiceRepository).save(invoice);
        verify(notificationService).notifyCustomerInvoiceCancelled(any(), any(), eq("INV-2026-00001"), eq("Duplicate invoice"));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceDoesNotExist() {
        UUID id = UUID.randomUUID();
        CancelInvoiceRequest request = new CancelInvoiceRequest("Some reason");

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> cancelInvoiceUseCase.execute(id, request)
        );

        assertEquals("Invoice not found with id: " + id, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_SaveExactlyOnce_When_CancellationSucceeds() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT);
        CancelInvoiceRequest request = new CancelInvoiceRequest("Test reason");

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        cancelInvoiceUseCase.execute(id, request);

        verify(invoiceRepository, times(1)).save(invoice);
        verify(notificationService).notifyCustomerInvoiceCancelled(any(), any(), any(), any());
    }

    @Test
    void should_ThrowException_When_InvoiceAlreadyCancelled() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.CANCELLED);
        CancelInvoiceRequest request = new CancelInvoiceRequest("Reason");

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> cancelInvoiceUseCase.execute(id, request)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_release_inventory_for_draft_cancellation() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        cancelInvoiceUseCase.execute(id, new CancelInvoiceRequest("Reason"));
        verify(inventoryGateway).cancelInvoice(argThat(command -> command.invoiceId().equals(id)
                && command.invoiceNumber().equals("INV-2026-00001")), eq(false));
    }

    @Test
    void should_restore_inventory_for_confirmed_cancellation() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID);
        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        cancelInvoiceUseCase.execute(id, new CancelInvoiceRequest("Reason"));
        verify(inventoryGateway).cancelInvoice(argThat(command -> command.invoiceId().equals(id)), eq(true));
    }

    @Test
    void should_stop_cancellation_side_effects_when_inventory_operation_fails() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        doThrow(new IllegalStateException("Missing stock")).when(inventoryGateway).cancelInvoice(any(), eq(false));
        assertThrows(IllegalStateException.class,
                () -> cancelInvoiceUseCase.execute(id, new CancelInvoiceRequest("Reason")));
        verify(invoiceRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }
    private Invoice buildInvoice(UUID id, InvoiceStatus status) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                null,
                null,
                status,
                PaymentType.CASH,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

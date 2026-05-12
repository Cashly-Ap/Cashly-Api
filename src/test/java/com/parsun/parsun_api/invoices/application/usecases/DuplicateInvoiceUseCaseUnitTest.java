package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuplicateInvoiceUseCase Unit Tests")
class DuplicateInvoiceUseCaseUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    private DuplicateInvoiceUseCase duplicateInvoiceUseCase;

    @BeforeEach
    void setUp() {
        duplicateInvoiceUseCase = new DuplicateInvoiceUseCase(invoiceRepository,
                warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_CreateDuplicate_When_OriginalInvoiceExists() {
        UUID originalId = UUID.randomUUID();
        Invoice original = buildInvoice(originalId);

        when(invoiceRepository.findById(InvoiceId.of(originalId))).thenReturn(Optional.of(original));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = duplicateInvoiceUseCase.execute(originalId);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        verify(invoiceRepository).findById(InvoiceId.of(originalId));
        verify(invoiceRepository).getNextSequenceForYear(anyInt());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_OriginalDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> duplicateInvoiceUseCase.execute(id)
        );

        assertEquals("Invoice not found with id: " + id, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_CopyLinesFromOriginal_When_Duplicating() {
        UUID originalId = UUID.randomUUID();
        Invoice original = buildInvoice(originalId);

        when(invoiceRepository.findById(InvoiceId.of(originalId))).thenReturn(Optional.of(original));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(3);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = duplicateInvoiceUseCase.execute(originalId);

        assertEquals(1, response.lines().size());
    }

    @Test
    void should_CreateDuplicateWithoutOrderReference_When_OriginalHasOrder() {
        UUID originalId = UUID.randomUUID();
        Invoice original = buildInvoiceWithOrder(originalId);

        when(invoiceRepository.findById(InvoiceId.of(originalId))).thenReturn(Optional.of(original));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(4);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = duplicateInvoiceUseCase.execute(originalId);

        assertNull(response.orderId());
        assertNull(response.orderNumber());
    }

    @Test
    void should_GenerateNewInvoiceNumber_When_Duplicating() {
        UUID originalId = UUID.randomUUID();
        Invoice original = buildInvoice(originalId);

        when(invoiceRepository.findById(InvoiceId.of(originalId))).thenReturn(Optional.of(original));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(99);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = duplicateInvoiceUseCase.execute(originalId);

        assertNotEquals(original.getInvoiceNumber().value(), response.invoiceNumber());
    }

    private Invoice buildInvoice(UUID id) {
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
                InvoiceStatus.DRAFT,
                PaymentType.CASH,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Invoice buildInvoiceWithOrder(UUID id) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product B", "PROD-002",
                BigDecimal.TWO, BigDecimal.valueOf(50), List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2026, 5),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                UUID.randomUUID(),
                "ORD-2026-00001",
                InvoiceStatus.DRAFT,
                PaymentType.CREDIT,
                "Some notes",
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

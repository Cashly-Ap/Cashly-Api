package com.parsun.parsun_api.returns.application.mappers;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.returns.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.returns.application.dto.response.ReturnListResponse;
import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnId;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnLineAllocation;
import com.parsun.parsun_api.returns.domain.entities.ReturnNumber;
import com.parsun.parsun_api.returns.domain.entities.ReturnType;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnMapper Unit Tests")
class ReturnMapperTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private ReturnMapper returnMapper;

    @BeforeEach
    void setUp() {
        returnMapper = new ReturnMapper(invoiceRepository, warehouseRepository);
    }

    @Test
    void should_MapToResponse_When_ReturnHasAllFields() {
        UUID invoiceId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);
        when(invoice.getInvoiceNumber()).thenReturn(InvoiceNumber.of(2026, 1));
        when(invoice.getCustomerName()).thenReturn("Cliente Test");
        when(invoice.getCustomerRif()).thenReturn("J-12345678-9");
        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.of(invoice));

        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getName()).thenReturn("Deposito Principal");
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        ReturnLineAllocation allocation = ReturnLineAllocation.create(warehouseId, BigDecimal.ONE);
        ReturnLine line = ReturnLine.create(
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                List.of(allocation)
        );
        Return return_ = Return.reconstitute(
                ReturnId.generate(), ReturnNumber.of(2026, 1),
                InvoiceId.of(invoiceId), UUID.randomUUID(),
                ReturnType.AUTOMATIC, "Producto defectuoso",
                new BigDecimal("50.00"), List.of(line), List.of(),
                UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        ReturnResponse response = returnMapper.toResponse(return_);

        assertNotNull(response);
        assertEquals(return_.getId().value(), response.id());
        assertEquals("RET-2026-0001", response.returnNumber());
        assertEquals("INV-2026-00001", response.invoiceNumber());
        assertEquals("Cliente Test", response.customerName());
        assertEquals("J-12345678-9", response.customerRif());
        assertEquals("AUTOMATIC", response.returnType());
        assertEquals("Producto defectuoso", response.reason());
        assertEquals(new BigDecimal("50.00"), response.total());
        assertEquals(1, response.lines().size());
        assertEquals(1, response.lines().get(0).allocations().size());
        assertEquals("Deposito Principal", response.lines().get(0).allocations().get(0).warehouseName());
        assertEquals("INV-2026-00001", response.invoiceNumber());
    }

    @Test
    void should_MapToResponse_When_InvoiceNotFound() {
        UUID invoiceId = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.empty());

        ReturnLine line = ReturnLine.create(
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                List.of()
        );
        Return return_ = Return.reconstitute(
                ReturnId.generate(), ReturnNumber.of(2026, 1),
                InvoiceId.of(invoiceId), UUID.randomUUID(),
                ReturnType.AUTOMATIC, "Producto defectuoso",
                new BigDecimal("50.00"), List.of(line), List.of(),
                UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        ReturnResponse response = returnMapper.toResponse(return_);

        assertNotNull(response);
        assertEquals("", response.invoiceNumber());
        assertEquals("", response.customerName());
        assertEquals("", response.customerRif());
    }

    @Test
    void should_MapToListResponse_When_ReturnHasAllFields() {
        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = mock(Invoice.class);
        when(invoice.getInvoiceNumber()).thenReturn(InvoiceNumber.of(2026, 1));
        when(invoice.getCustomerName()).thenReturn("Cliente Test");
        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.of(invoice));

        ReturnLine line = ReturnLine.create(
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                List.of()
        );
        Return return_ = Return.reconstitute(
                ReturnId.generate(), ReturnNumber.of(2026, 1),
                InvoiceId.of(invoiceId), UUID.randomUUID(),
                ReturnType.MANUAL, "Cambio de producto",
                new BigDecimal("50.00"), List.of(line), List.of(),
                UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        ReturnListResponse response = returnMapper.toListResponse(return_);

        assertNotNull(response);
        assertEquals(return_.getId().value(), response.id());
        assertEquals("RET-2026-0001", response.returnNumber());
        assertEquals("INV-2026-00001", response.invoiceNumber());
        assertEquals("Cliente Test", response.customerName());
        assertEquals("MANUAL", response.returnType());
        assertEquals("Cambio de producto", response.reason());
        assertEquals(new BigDecimal("50.00"), response.total());
        assertEquals(1, response.lineCount());
        assertEquals("INV-2026-00001", response.invoiceNumber());
    }

    @Test
    void should_MapToListResponse_When_InvoiceNotFound() {
        UUID invoiceId = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.empty());

        ReturnLine line = ReturnLine.create(
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                List.of()
        );
        Return return_ = Return.reconstitute(
                ReturnId.generate(), ReturnNumber.of(2026, 1),
                InvoiceId.of(invoiceId), UUID.randomUUID(),
                ReturnType.AUTOMATIC, "Producto defectuoso",
                new BigDecimal("50.00"), List.of(line), List.of(),
                UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        ReturnListResponse response = returnMapper.toListResponse(return_);

        assertNotNull(response);
        assertEquals("", response.invoiceNumber());
        assertEquals("", response.customerName());
    }

    @Test
    void should_ConvertRequestLineToReturnLine_When_ValidRequest() {
        UUID warehouseId = UUID.randomUUID();
        CreateReturnRequest.ReturnLineRequest request = new CreateReturnRequest.ReturnLineRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                null,
                List.of(new CreateReturnRequest.ReturnLineRequest.AllocationRequest(
                        warehouseId, BigDecimal.ONE
                ))
        );

        ReturnLine line = ReturnMapper.toLine(request);

        assertNotNull(line);
        assertEquals(request.invoiceLineId(), line.getInvoiceLineId());
        assertEquals(request.productId(), line.getProductId());
        assertEquals(request.quantity(), line.getQuantity());
        assertEquals(request.unitPrice(), line.getUnitPrice());
        assertEquals(request.subtotal(), line.getSubtotal());
        assertEquals(1, line.getAllocations().size());
        assertEquals(warehouseId, line.getAllocations().get(0).getWarehouseId());
        assertEquals(BigDecimal.ONE, line.getAllocations().get(0).getAllocated());
    }

    @Test
    void should_ConvertRequestLineToReturnLine_When_NoAllocations() {
        CreateReturnRequest.ReturnLineRequest request = new CreateReturnRequest.ReturnLineRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                null, List.of()
        );

        ReturnLine line = ReturnMapper.toLine(request);

        assertNotNull(line);
        assertTrue(line.getAllocations().isEmpty());
    }
}

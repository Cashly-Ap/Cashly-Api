package com.cashly.cashly_api.returns.application.services;

import com.cashly.cashly_api.returns.application.contracts.ReturnProjectionQueryGateway;
import com.cashly.cashly_api.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnLine;
import com.cashly.cashly_api.returns.domain.entities.ReturnLineAllocation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class ReturnResponseAssemblerTest {
    private final ReturnProjectionQueryGateway projections = mock(ReturnProjectionQueryGateway.class);
    private final ReturnMapper mapper = mock(ReturnMapper.class);
    private final ReturnResponseAssembler assembler = new ReturnResponseAssembler(projections, mapper);

    @Test
    void shouldResolveDistinctWarehousesAndInvoiceBeforeMapping() {
        UUID invoiceId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Return return_ = mock(Return.class);
        ReturnLine line = mock(ReturnLine.class);
        ReturnLineAllocation allocation1 = mock(ReturnLineAllocation.class);
        ReturnLineAllocation allocation2 = mock(ReturnLineAllocation.class);
        var labels = new ReturnProjectionQueryGateway.InvoiceLabels("INV-1", "Customer", "J-1");
        when(return_.getInvoiceId()).thenReturn(invoiceId);
        when(return_.getLines()).thenReturn(List.of(line));
        when(line.getAllocations()).thenReturn(List.of(allocation1, allocation2));
        when(allocation1.getWarehouseId()).thenReturn(warehouseId);
        when(allocation2.getWarehouseId()).thenReturn(warehouseId);
        when(projections.findInvoice(invoiceId)).thenReturn(Optional.of(labels));
        when(projections.findWarehouseName(warehouseId)).thenReturn(Optional.of("Main"));

        assembler.toResponse(return_);

        verify(projections).findWarehouseName(warehouseId);
        verify(mapper).toResponse(return_, labels, Map.of(warehouseId, "Main"));
    }

    @Test
    void shouldPropagateProjectionErrorsWithoutMappingPartialData() {
        UUID invoiceId = UUID.randomUUID();
        Return return_ = mock(Return.class);
        IllegalStateException failure = new IllegalStateException("invoice query unavailable");
        when(return_.getInvoiceId()).thenReturn(invoiceId);
        when(projections.findInvoice(invoiceId)).thenThrow(failure);

        assertThatThrownBy(() -> assembler.toResponse(return_)).isSameAs(failure);
        verifyNoInteractions(mapper);
    }
}

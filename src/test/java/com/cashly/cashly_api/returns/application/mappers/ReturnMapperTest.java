package com.cashly.cashly_api.returns.application.mappers;

import com.cashly.cashly_api.returns.application.ports.out.ReturnProjectionQueryGateway;
import com.cashly.cashly_api.returns.application.dto.request.CreateReturnRequest;
import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnLine;
import com.cashly.cashly_api.returns.domain.entities.ReturnLineAllocation;
import com.cashly.cashly_api.returns.domain.entities.ReturnNumber;
import com.cashly.cashly_api.returns.domain.entities.ReturnType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnMapperTest {
    private final ReturnMapper mapper = new ReturnMapper();

    @Test
    void shouldMapResolvedInvoiceAndWarehouseLabels() {
        UUID invoiceId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Return returned = returned(invoiceId, List.of(ReturnLine.create(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN,
                List.of(ReturnLineAllocation.create(warehouseId, BigDecimal.ONE)))));
        var invoice = new ReturnProjectionQueryGateway.InvoiceLabels("INV-1", "Customer", "J-1");

        ReturnResponse response = mapper.toResponse(returned, invoice, Map.of(warehouseId, "Main"));

        assertThat(response.invoiceNumber()).isEqualTo("INV-1");
        assertThat(response.customerName()).isEqualTo("Customer");
        assertThat(response.customerRif()).isEqualTo("J-1");
        assertThat(response.lines().getFirst().allocations().getFirst().warehouseName()).isEqualTo("Main");
    }

    @Test
    void shouldKeepEmptyLabelsWhenProjectionWasNotFound() {
        Return returned = returned(UUID.randomUUID(), List.of());

        ReturnResponse response = mapper.toResponse(returned, null, Map.of());
        ReturnListResponse listResponse = mapper.toListResponse(returned, null);

        assertThat(response.invoiceNumber()).isEmpty();
        assertThat(response.customerName()).isEmpty();
        assertThat(response.customerRif()).isEmpty();
        assertThat(listResponse.invoiceNumber()).isEmpty();
        assertThat(listResponse.customerName()).isEmpty();
    }

    @Test
    void shouldConvertRequestLineWithoutInfrastructureReads() {
        UUID warehouseId = UUID.randomUUID();
        var request = new CreateReturnRequest.ReturnLineRequest(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, null,
                List.of(new CreateReturnRequest.ReturnLineRequest.AllocationRequest(warehouseId, BigDecimal.ONE)));

        ReturnLine line = ReturnMapper.toLine(request);

        assertThat(line.getAllocations()).extracting(ReturnLineAllocation::getWarehouseId).containsExactly(warehouseId);
    }

    private Return returned(UUID invoiceId, List<ReturnLine> lines) {
        return Return.reconstitute(com.cashly.cashly_api.returns.domain.entities.ReturnId.generate(),
                ReturnNumber.of(2026, 1), invoiceId, UUID.randomUUID(), ReturnType.AUTOMATIC, "Reason",
                BigDecimal.TEN, lines, List.of(), UUID.randomUUID(), "Operator",
                LocalDateTime.now(), LocalDateTime.now());
    }
}

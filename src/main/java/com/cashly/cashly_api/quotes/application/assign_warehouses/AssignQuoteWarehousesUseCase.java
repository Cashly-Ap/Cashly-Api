package com.cashly.cashly_api.quotes.application.assign_warehouses;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.assign_warehouses.AssignQuoteWarehousesRequest;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.Quote;
import com.cashly.cashly_api.quotes.domain.QuoteAllocation;
import com.cashly.cashly_api.quotes.domain.QuoteLine;
import com.cashly.cashly_api.quotes.domain.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteLineId;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class AssignQuoteWarehousesUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteWarehouseGateway warehouses;

    public AssignQuoteWarehousesUseCase(QuoteRepository quoteRepository, QuoteWarehouseGateway warehouses) {
        this.quoteRepository = quoteRepository;
        this.warehouses = warehouses;
    }

    @Transactional
    public QuoteDetailResponse execute(UUID quoteId, AssignQuoteWarehousesRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        UUID customerId = quote.getCustomerId();

        for (AssignQuoteWarehousesRequest.LineAllocationRequest lineRequest : request.lines()) {
            QuoteLineId lineId = QuoteLineId.of(lineRequest.lineId());
            QuoteLine line = quote.getLines().stream()
                    .filter(candidate -> candidate.getId().equals(lineId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Quote line not found: " + lineRequest.lineId()));

            BigDecimal lineAllocatedTotal = BigDecimal.ZERO;
            for (AssignQuoteWarehousesRequest.WarehouseAllocationRequest allocationRequest : lineRequest.allocations()) {
                assertCustomerWarehouseAccess(customerId, allocationRequest.warehouseId());

                String warehouseName = warehouses.findById(allocationRequest.warehouseId())
                        .map(QuoteWarehouseGateway.Warehouse::name)
                        .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + allocationRequest.warehouseId()));

                QuoteAllocation allocation = new QuoteAllocation(line.getProductId(), allocationRequest.warehouseId(),
                        warehouseName, allocationRequest.allocatedQuantity());
                quote.assignAllocation(lineId, allocation);
                lineAllocatedTotal = lineAllocatedTotal.add(allocationRequest.allocatedQuantity());
            }

            if (lineAllocatedTotal.compareTo(line.getQuantity()) != 0) {
                throw new IllegalArgumentException(
                        "Allocated quantity must equal line quantity for line " + lineRequest.lineId());
            }
        }

        quote.confirmWarehouseAssignment();

        Quote saved = quoteRepository.save(quote);
        return QuoteMapper.toDetailResponse(saved);
    }

    private void assertCustomerWarehouseAccess(UUID customerId, UUID warehouseId) {
        if (!warehouses.accessibleWarehouseIds(customerId).contains(warehouseId)) {
            throw new IllegalArgumentException(
                    "Customer " + customerId + " does not have access to warehouse " + warehouseId);
        }
    }
}

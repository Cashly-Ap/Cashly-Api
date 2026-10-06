package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.quotes.application.dto.request.AssignQuoteWarehousesRequest;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.entities.QuoteAllocation;
import com.cashly.cashly_api.quotes.domain.entities.QuoteLine;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteLineId;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class AssignQuoteWarehousesUseCase {

    private final QuoteRepository quoteRepository;
    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;
    private final WarehouseRepository warehouseRepository;

    public AssignQuoteWarehousesUseCase(QuoteRepository quoteRepository,
                                        CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
                                        WarehouseRepository warehouseRepository) {
        this.quoteRepository = quoteRepository;
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public QuoteDetailResponse execute(UUID quoteId, AssignQuoteWarehousesRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        UUID customerId = quote.getCustomerId();

        for (AssignQuoteWarehousesRequest.LineAllocationRequest lineRequest : request.lines()) {
            QuoteLineId lineId = QuoteLineId.of(lineRequest.lineId());
            QuoteLine line = quote.getLines().stream()
                    .filter(l -> l.getId().equals(lineId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Quote line not found: " + lineRequest.lineId()));

            BigDecimal lineAllocatedTotal = BigDecimal.ZERO;
            for (AssignQuoteWarehousesRequest.WarehouseAllocationRequest allocationRequest : lineRequest.allocations()) {
                assertCustomerWarehouseAccess(customerId, allocationRequest.warehouseId());

                String warehouseName = warehouseRepository.findById(allocationRequest.warehouseId())
                        .map(w -> w.getName())
                        .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + allocationRequest.warehouseId()));

                QuoteAllocation allocation = new QuoteAllocation(
                        line.getProductId(),
                        allocationRequest.warehouseId(),
                        warehouseName,
                        allocationRequest.allocatedQuantity()
                );

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
        boolean hasAccess = customerWarehouseAccessRepository
                .existsByCustomerIdAndWarehouseId(customerId, warehouseId);
        if (!hasAccess) {
            throw new IllegalArgumentException(
                    "Customer " + customerId + " does not have access to warehouse " + warehouseId);
        }
    }
}

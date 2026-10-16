package com.cashly.cashly_api.quotes.application.update;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.create.CreateQuoteRequest;
import com.cashly.cashly_api.quotes.application.update.UpdateQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.Quote;
import com.cashly.cashly_api.quotes.domain.QuoteAllocation;
import com.cashly.cashly_api.quotes.domain.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.quotes.domain.QuoteRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteProductCatalogGateway products;
    private final QuoteWarehouseGateway warehouses;

    public UpdateQuoteUseCase(QuoteRepository quoteRepository, QuoteProductCatalogGateway products,
                              QuoteWarehouseGateway warehouses) {
        this.quoteRepository = quoteRepository;
        this.products = products;
        this.warehouses = warehouses;
    }

    @Transactional
    public QuoteDetailResponse execute(UUID id, UpdateQuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        if (!quote.canBeModified()) {
            throw new QuoteStateException("Quote can only be modified in DRAFT status");
        }

        List<QuoteLine> oldLines = new ArrayList<>(quote.getLines());
        for (QuoteLine oldLine : oldLines) {
            quote.removeLine(oldLine.getId());
        }

        for (CreateQuoteRequest.QuoteLineRequest lineRequest : request.lines()) {
            var product = products.findProduct(lineRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

            QuoteLine newLine = new QuoteLine(product.id(), product.name(), product.code(), product.categoryId(),
                    lineRequest.quantity(), lineRequest.unitPrice());

            List<CreateQuoteRequest.WarehouseAllocationRequest> allocationRequests =
                    lineRequest.warehouseAllocations() != null ? lineRequest.warehouseAllocations() : List.of();

            allocationRequests.forEach(allocationRequest -> {
                String warehouseName = warehouses.findById(allocationRequest.warehouseId())
                        .map(QuoteWarehouseGateway.Warehouse::name)
                        .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + allocationRequest.warehouseId()));

                QuoteAllocation allocation = new QuoteAllocation(
                        product.id(),
                        allocationRequest.warehouseId(),
                        warehouseName,
                        allocationRequest.allocatedQuantity()
                );
                newLine.addAllocation(allocation);
            });

            quote.addLine(newLine);
        }

        Quote saved = quoteRepository.save(quote);
        return QuoteMapper.toDetailResponse(saved);
    }
}

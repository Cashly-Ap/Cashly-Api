package com.cashly.cashly_api.sales.quotes.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.sales.quotes.application.create.request.CreateQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.update.request.UpdateQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.sales.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteAllocation;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteProductCatalogGateway products;
    private final QuoteWarehouseGateway warehouses;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateQuoteUseCase(QuoteRepository quoteRepository,
            QuoteProductCatalogGateway products,
            QuoteWarehouseGateway warehouses,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.quoteRepository = quoteRepository;
        this.products = products;
        this.warehouses = warehouses;
    }

    @Transactional
    public QuoteDetailResponse execute(UUID id, UpdateQuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        if (!quote.canBeModified()) {
            throw new QuoteStateException(
                    "Quote can only be modified in DRAFT status");
        }

        List<QuoteLine> oldLines = new ArrayList<>(quote.getLines());
        for (QuoteLine oldLine : oldLines) {
            quote.removeLine(oldLine.getId());
        }

        for (CreateQuoteRequest.QuoteLineRequest lineRequest : request
                .lines()) {
            var product = products.findProduct(lineRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + lineRequest.productId()));

            QuoteLine newLine = new QuoteLine(product.id(), product.name(),
                    product.code(), product.categoryId(),
                    lineRequest.quantity(), lineRequest.unitPrice());

            List<CreateQuoteRequest.WarehouseAllocationRequest> allocationRequests = lineRequest
                    .warehouseAllocations() != null
                            ? lineRequest.warehouseAllocations()
                            : List.of();

            allocationRequests.forEach(allocationRequest -> {
                String warehouseName = warehouses
                        .findById(allocationRequest.warehouseId())
                        .map(QuoteWarehouseGateway.Warehouse::name)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Warehouse not found: "
                                        + allocationRequest.warehouseId()));

                QuoteAllocation allocation = new QuoteAllocation(product.id(),
                        allocationRequest.warehouseId(), warehouseName,
                        allocationRequest.allocatedQuantity());
                newLine.addAllocation(allocation);
            });

            quote.addLine(newLine);
        }

        Quote saved = quoteRepository.save(quote);
        activityNotifications.record(ActivityEvent.of("SALES_QUOTES",
                "EDIT", "QUOTE", saved.getId(),
                saved.getQuoteNumber().value(), null));
        return QuoteMapper.toDetailResponse(saved);
    }
}

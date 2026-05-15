package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.request.CreateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.request.UpdateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.mappers.QuoteMapper;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public UpdateQuoteUseCase(QuoteRepository quoteRepository,
                               ProductRepository productRepository,
                               WarehouseRepository warehouseRepository) {
        this.quoteRepository = quoteRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
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
            Product product = productRepository.findById(lineRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

            QuoteLine newLine = new QuoteLine(
                    product.getId(),
                    product.getName(),
                    product.getCode(),
                    lineRequest.quantity(),
                    lineRequest.unitPrice()
            );

            lineRequest.warehouseAllocations().forEach(allocationRequest -> {
                String warehouseName = warehouseRepository.findById(allocationRequest.warehouseId())
                        .map(w -> w.getName())
                        .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + allocationRequest.warehouseId()));

                QuoteAllocation allocation = new QuoteAllocation(
                        product.getId(),
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

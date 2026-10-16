package com.cashly.cashly_api.quotes.application.create.use_case;

import com.cashly.cashly_api.quotes.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteCustomerGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.create.request.CreateQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.entity.Quote;
import com.cashly.cashly_api.quotes.domain.entity.QuoteAllocation;
import com.cashly.cashly_api.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateQuoteUseCase {
    private final QuoteRepository quoteRepository;
    private final QuoteCustomerGateway customers;
    private final ActorIdentityGateway actors;
    private final QuoteProductCatalogGateway products;
    private final QuoteWarehouseGateway warehouses;

    public CreateQuoteUseCase(QuoteRepository quoteRepository, QuoteCustomerGateway customers,
                              ActorIdentityGateway actors, QuoteProductCatalogGateway products,
                              QuoteWarehouseGateway warehouses) {
        this.quoteRepository = quoteRepository;
        this.customers = customers;
        this.actors = actors;
        this.products = products;
        this.warehouses = warehouses;
    }

    @Transactional
    public QuoteDetailResponse execute(CreateQuoteRequest request, UUID authenticatedUserId) {
        QuoteCustomerGateway.Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));
        ActorIdentityGateway.Actor actor = actors.findActor(authenticatedUserId).orElse(null);
        UUID createdById = actor == null ? authenticatedUserId : actor.id();
        String createdByName = actor == null ? customer.legalName() : actor.name();
        List<QuoteLine> lines = buildLines(request);

        int year = LocalDate.now().getYear();
        QuoteNumber quoteNumber = QuoteNumber.of(year, quoteRepository.getNextSequenceForYear(year));
        Quote quote = Quote.create(quoteNumber, customer.id(), customer.legalName(), customer.rif(),
                createdById, createdByName, request.expiryDate(), request.notes(), lines);
        return QuoteMapper.toDetailResponse(quoteRepository.save(quote));
    }

    private List<QuoteLine> buildLines(CreateQuoteRequest request) {
        return request.lines().stream().map(lineRequest -> {
            QuoteProductCatalogGateway.ProductReference product = products.findProduct(lineRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));
            QuoteLine line = new QuoteLine(product.id(), product.name(), product.code(),
                    product.categoryId(), lineRequest.quantity(), lineRequest.unitPrice());
            List<CreateQuoteRequest.WarehouseAllocationRequest> allocationRequests =
                    lineRequest.warehouseAllocations() == null ? List.of() : lineRequest.warehouseAllocations();
            if (allocationRequests.isEmpty()) {
                throw new IllegalArgumentException("At least one warehouse allocation is required for product "
                        + product.code());
            }
            BigDecimal allocatedTotal = BigDecimal.ZERO;
            for (CreateQuoteRequest.WarehouseAllocationRequest allocationRequest : allocationRequests) {
                String warehouseName = warehouses.findById(allocationRequest.warehouseId())
                        .map(QuoteWarehouseGateway.Warehouse::name)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Warehouse not found: " + allocationRequest.warehouseId()));
                line.addAllocation(new QuoteAllocation(product.id(), allocationRequest.warehouseId(),
                        warehouseName, allocationRequest.allocatedQuantity()));
                allocatedTotal = allocatedTotal.add(allocationRequest.allocatedQuantity());
            }
            if (allocatedTotal.compareTo(lineRequest.quantity()) != 0) {
                throw new IllegalArgumentException("Allocated quantity must equal line quantity for product "
                        + product.code());
            }
            return line;
        }).toList();
    }
}

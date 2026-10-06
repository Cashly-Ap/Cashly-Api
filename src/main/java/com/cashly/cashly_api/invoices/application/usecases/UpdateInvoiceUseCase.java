package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.invoices.application.dto.request.UpdateInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.shared.application.StockEnrichmentService;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;

@Service
public class UpdateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockEnrichmentService stockEnrichmentService;
    private final CustomerRepository customerRepository;

    @Autowired
    public UpdateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                ProductRepository productRepository,
                                WarehouseStockRepository warehouseStockRepository,
                                StockEnrichmentService stockEnrichmentService,
                                CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockEnrichmentService = stockEnrichmentService;
        this.customerRepository = customerRepository;
    }

    public UpdateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                ProductRepository productRepository,
                                WarehouseStockRepository warehouseStockRepository,
                                StockEnrichmentService stockEnrichmentService) {
        this(invoiceRepository, productRepository, warehouseStockRepository, stockEnrichmentService, null);
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        revertStockAllocations(invoice);

        List<InvoiceLine> newLines = request.lines().stream()
                .map(lineRequest -> {
                    Product product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Product not found: " + lineRequest.productId()));

                    List<InvoiceLineAllocation> allocations = applyStockAllocations(
                            lineRequest, product.getId());

                    return InvoiceLine.create(
                            product.getId(),
                            product.getName(),
                            product.getCode(),
                            lineRequest.quantity(),
                            lineRequest.unitPrice(),
                            allocations
                    );
                })
                .toList();

        PaymentType paymentType = PaymentType.valueOf(request.paymentType());
        invoice.update(paymentType, request.notes(), newLines);
        if (request.issueDate() != null && customerRepository != null) {
            Customer customer = customerRepository.findById(new CustomerId(invoice.getCustomerId()))
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + invoice.getCustomerId()));
            invoice.updateDates(request.issueDate(), request.issueDate().plusDays(customer.getCreditDays().getValue()));
        }

        Invoice saved = invoiceRepository.save(invoice);

        List<StockEnrichmentService.AllocationData> allocations = saved.getLines().stream()
                .flatMap(line -> line.getAllocations().stream()
                        .map(a -> new StockEnrichmentService.AllocationData(
                                a.getWarehouseId(),
                                line.getProductId(),
                                a.getAllocated().intValue()
                        )))
                .toList();

        List<StockEnrichmentService.EnrichedStock> enriched = stockEnrichmentService.enrich(allocations);

        Map<InvoiceMapper.WarehouseProductKey, InvoiceMapper.WarehouseEnrichment> enrichmentMap = new HashMap<>();
        for (StockEnrichmentService.EnrichedStock es : enriched) {
            enrichmentMap.put(
                    new InvoiceMapper.WarehouseProductKey(es.warehouseId(), es.productId()),
                    new InvoiceMapper.WarehouseEnrichment(es.warehouseCode(), es.warehouseName(), es.availableStock())
            );
        }

        return InvoiceMapper.toDetailResponse(saved, enrichmentMap, Map.of());
    }

    private void revertStockAllocations(Invoice invoice) {
        for (InvoiceLine line : invoice.getLines()) {
            for (InvoiceLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            stock.releaseReserved(allocation.getAllocated().intValue());
                            warehouseStockRepository.save(stock);
                        });
            }
        }
    }

    private List<InvoiceLineAllocation> applyStockAllocations(
            UpdateInvoiceRequest.InvoiceLineRequest lineRequest, UUID productId) {

        List<InvoiceLineAllocation> allocations = new ArrayList<>();
        for (UpdateInvoiceRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.warehouseId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for warehouse=" + allocation.warehouseId() + " product=" + productId));

            stock.reserve(allocation.allocated().intValue());
            warehouseStockRepository.save(stock);

            allocations.add(InvoiceLineAllocation.create(allocation.warehouseId(), allocation.allocated()));
        }
        return allocations;
    }
}

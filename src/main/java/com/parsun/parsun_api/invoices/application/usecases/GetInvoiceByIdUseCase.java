package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetInvoiceByIdUseCase {

    private final InvoiceRepository invoiceRepository;
    private final StockEnrichmentService stockEnrichmentService;

    public GetInvoiceByIdUseCase(InvoiceRepository invoiceRepository,
                                  StockEnrichmentService stockEnrichmentService) {
        this.invoiceRepository = invoiceRepository;
        this.stockEnrichmentService = stockEnrichmentService;
    }

    public InvoiceDetailResponse execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        List<StockEnrichmentService.AllocationData> allocations = invoice.getLines().stream()
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

        List<UUID> productIds = invoice.getLines().stream()
                .map(InvoiceLine::getProductId)
                .toList();
        Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks =
                stockEnrichmentService.getAllStockByProducts(invoice.getCustomerId(), productIds);

        return InvoiceMapper.toDetailResponse(invoice, enrichmentMap, productStocks);
    }
}

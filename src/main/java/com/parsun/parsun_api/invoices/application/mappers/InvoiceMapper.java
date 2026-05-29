package com.parsun.parsun_api.invoices.application.mappers;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvoiceMapper {

    public record WarehouseProductKey(UUID warehouseId, UUID productId) {}

    public record WarehouseEnrichment(String code, String name, int availableStock) {}

    private InvoiceMapper() {}

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice,
                                                           Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
                                                           Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks,
                                                           Map<WarehouseProductKey, Integer> thisInvoiceAllocations) {
        return toDetailResponse(invoice, enrichmentMap, productStocks, thisInvoiceAllocations,
                BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice,
                                                           Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
                                                           Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks,
                                                           Map<WarehouseProductKey, Integer> thisInvoiceAllocations,
                                                           BigDecimal creditApplied,
                                                           BigDecimal debitApplied,
                                                           List<InvoiceDetailResponse.NoteInfo> notes,
                                                           List<InvoiceDetailResponse.ReturnDetail> returns) {
        List<InvoiceDetailResponse.InvoiceLineDetailResponse> lineResponses = invoice.getLines().stream()
                .map(line -> toLineDetailResponse(line, enrichmentMap, productStocks, thisInvoiceAllocations))
                .toList();

        BigDecimal total;
        if (invoice.getReturnStatus() != null
                && invoice.getReturnStatus().name().equals("FULL")) {
            total = BigDecimal.ZERO;
        } else {
            total = invoice.getSubtotal()
                    .subtract(invoice.getReturnedTotal())
                    .subtract(creditApplied)
                    .add(debitApplied);
        }

        String returnStatus = invoice.getReturnStatus() != null ? invoice.getReturnStatus().name() : null;

        return new InvoiceDetailResponse(
                invoice.getId().value(),
                invoice.getInvoiceNumber().value(),
                invoice.getCustomerId(),
                invoice.getCustomerName(),
                invoice.getCustomerRif(),
                invoice.getCreatedBy(),
                invoice.getCreatedByName(),
                invoice.getOrderId(),
                invoice.getOrderNumber(),
                invoice.getStatus().name(),
                invoice.getPaymentType().name(),
                invoice.getNotes(),
                invoice.getSubtotal(),
                creditApplied,
                debitApplied,
                total,
                notes,
                invoice.getCancelledAt(),
                invoice.getCancelledReason(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                lineResponses,
                invoice.getReturnedTotal(),
                returnStatus,
                returns
        );
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice,
                                                           BigDecimal creditApplied,
                                                           BigDecimal debitApplied,
                                                           List<InvoiceDetailResponse.NoteInfo> notes) {
        return toDetailResponse(invoice, Map.of(), Map.of(), Map.of(),
                creditApplied, debitApplied, notes, List.of());
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice,
                                                           Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
                                                           Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks) {
        return toDetailResponse(invoice, enrichmentMap, productStocks, Map.of());
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice) {
        return toDetailResponse(invoice, Map.of(), Map.of(), Map.of());
    }

    public static InvoiceListResponse toListResponse(Invoice invoice) {
        String returnStatus = invoice.getReturnStatus() != null ? invoice.getReturnStatus().name() : null;

        return new InvoiceListResponse(
                invoice.getId().value(),
                invoice.getInvoiceNumber().value(),
                invoice.getCustomerId(),
                invoice.getCustomerName(),
                invoice.getStatus().name(),
                invoice.getPaymentType().name(),
                invoice.getSubtotal(),
                invoice.getLines().size(),
                invoice.getOrderId(),
                invoice.getOrderNumber(),
                invoice.getCreatedAt(),
                returnStatus
        );
    }

    private static InvoiceDetailResponse.InvoiceLineDetailResponse toLineDetailResponse(
            InvoiceLine line, Map<WarehouseProductKey, WarehouseEnrichment> enrichmentMap,
            Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks,
            Map<WarehouseProductKey, Integer> thisInvoiceAllocations) {

        List<InvoiceDetailResponse.WarehouseAllocationResponse> allocationDetails =
                line.getAllocations().stream()
                        .map(a -> {
                            WarehouseProductKey key = new WarehouseProductKey(a.getWarehouseId(), line.getProductId());
                            WarehouseEnrichment enrichment = enrichmentMap.get(key);
                            String code = enrichment != null ? enrichment.code() : null;
                            String name = enrichment != null ? enrichment.name() : null;
                            int availableStock = enrichment != null ? enrichment.availableStock() : 0;
                            return new InvoiceDetailResponse.WarehouseAllocationResponse(
                                    a.getId(),
                                    a.getWarehouseId(),
                                    code,
                                    name,
                                    availableStock,
                                    a.getAllocated()
                            );
                        })
                        .toList();

        List<InvoiceDetailResponse.WarehouseStockDetail> stockDetails =
                productStocks.getOrDefault(line.getProductId(), List.of()).stream()
                        .map(s -> {
                            WarehouseProductKey key = new WarehouseProductKey(s.warehouseId(), line.getProductId());
                            int thisAllocation = thisInvoiceAllocations.getOrDefault(key, 0);
                            return new InvoiceDetailResponse.WarehouseStockDetail(
                                    s.warehouseId(),
                                    s.warehouseCode(),
                                    s.warehouseName(),
                                    s.quantity(),
                                    Math.max(0, s.reserved() - thisAllocation),
                                    s.available() + thisAllocation
                            );
                        })
                        .toList();

        return new InvoiceDetailResponse.InvoiceLineDetailResponse(
                line.getId().value(),
                line.getProductId(),
                line.getProductName(),
                line.getProductCode(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal(),
                allocationDetails,
                stockDetails,
                line.getReturnedQuantity()
        );
    }
}

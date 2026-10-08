package com.cashly.cashly_api.quotes.application.mappers;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteAllocationDetailResponse;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteLineDetailResponse;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.quotes.application.dto.response.WarehouseStockInfoResponse;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.entities.QuoteAllocation;
import com.cashly.cashly_api.quotes.domain.entities.QuoteLine;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuoteMapper {

    private QuoteMapper() {}

    public static QuoteListResponse toListResponse(Quote quote) {
        return new QuoteListResponse(
                quote.getId(),
                quote.getQuoteNumber().value(),
                quote.getCustomerId(),
                quote.getCustomerName(),
                quote.getCustomerRif(),
                quote.getStatus().name(),
                quote.getExpiryDate(),
                quote.getSubtotal(),
                quote.getCreatedBy(),
                quote.getCreatedByName(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }

    public static QuoteDetailResponse toDetailResponse(Quote quote) {
        return toDetailResponse(quote, Map.of());
    }

    public static QuoteDetailResponse toDetailResponse(Quote quote,
                                                        Map<UUID, List<WarehouseStockInfo>> stockByProduct) {
        List<QuoteLineDetailResponse> lineResponses = quote.getLines().stream()
                .map(line -> toLineDetailResponse(line, stockByProduct.get(line.getProductId())))
                .toList();

        return new QuoteDetailResponse(
                quote.getId(),
                quote.getQuoteNumber().value(),
                quote.getCustomerId(),
                quote.getCustomerName(),
                quote.getCustomerRif(),
                quote.getCreatedBy(),
                quote.getCreatedByName(),
                quote.getStatus().name(),
                quote.getExpiryDate(),
                quote.getNotes(),
                quote.getSubtotal(),
                quote.getOrderId(),
                quote.getOrderNumber(),
                quote.getInvoiceId(),
                quote.getInvoiceNumber(),
                quote.getCancelledAt(),
                quote.getCancelledReason(),
                quote.getCreatedAt(),
                quote.getUpdatedAt(),
                lineResponses
        );
    }

    private static QuoteLineDetailResponse toLineDetailResponse(QuoteLine line, List<WarehouseStockInfo> stockInfo) {
        List<QuoteAllocationDetailResponse> allocationResponses = line.getAllocations().stream()
                .map(QuoteMapper::toAllocationDetailResponse)
                .toList();
        List<WarehouseStockInfoResponse> warehouseStocks = stockInfo != null
                ? stockInfo.stream()
                        .map(si -> new WarehouseStockInfoResponse(si.warehouseId(), si.warehouseName(), si.available()))
                        .toList()
                : List.of();
        return new QuoteLineDetailResponse(
                line.getId().value(),
                line.getProductId(),
                line.getProductName(),
                line.getProductCode(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal(),
                allocationResponses,
                warehouseStocks
        );
    }

    private static QuoteAllocationDetailResponse toAllocationDetailResponse(QuoteAllocation allocation) {
        return new QuoteAllocationDetailResponse(
                allocation.getId().value(), allocation.getWarehouseId(), allocation.getWarehouseName(),
                allocation.getAllocatedQuantity());
    }

    public record WarehouseStockInfo(UUID warehouseId, String warehouseName, int available) {}
}

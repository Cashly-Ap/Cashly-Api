package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.portal.application.dto.response.PortalQuoteDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalQuoteItemResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalQuoteListResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;

import java.math.BigDecimal;
import java.util.List;

public final class PortalQuoteMapper {

    private PortalQuoteMapper() {}

    public static PortalQuoteListResponse toListResponse(Quote quote) {
        return new PortalQuoteListResponse(
            quote.getId(),
            quote.getQuoteNumber().value(),
            quote.getQuoteNumber().value(),
            quote.getExpiryDate(),
            quote.getExpiryDate(),
            quote.getStatus().name(),
            quote.getSubtotal(),
            quote.getSubtotal(),
            quote.getLines().size(),
            quote.getCreatedAt()
        );
    }

    public static PortalQuoteDetailResponse toDetailResponse(Quote quote) {
        List<PortalQuoteItemResponse> items = quote.getLines().stream()
            .map(PortalQuoteMapper::toItemResponse)
            .toList();

        BigDecimal tax = quote.getSubtotal().multiply(new BigDecimal("0.16"));

        return new PortalQuoteDetailResponse(
            quote.getId(),
            quote.getQuoteNumber().value(),
            quote.getQuoteNumber().value(),
            quote.getExpiryDate(),
            quote.getStatus().name(),
            quote.getSubtotal(),
            quote.getSubtotal(),
            tax,
            quote.getNotes(),
            quote.getExpiryDate(),
            quote.getOrderNumber(),
            quote.getCreatedByName(),
            quote.getCreatedAt(),
            items,
            items
        );
    }

    private static PortalQuoteItemResponse toItemResponse(QuoteLine line) {
        return new PortalQuoteItemResponse(
            line.getProductId(),
            line.getProductCode(),
            line.getProductName(),
            line.getQuantity(),
            line.getUnitPrice(),
            line.getLineTotal()
        );
    }
}

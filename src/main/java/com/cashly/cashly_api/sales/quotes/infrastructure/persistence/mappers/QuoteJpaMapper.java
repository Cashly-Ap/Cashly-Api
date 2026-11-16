package com.cashly.cashly_api.sales.quotes.infrastructure.persistence.mappers;

import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteAllocationJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteLineJpaEntity;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteAllocation;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteAllocationId;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteLineId;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;

import java.util.List;

public class QuoteJpaMapper {

    public QuoteJpaMapper() {
    }

    public QuoteJpaEntity toJpa(Quote domain) {
        QuoteJpaEntity entity = new QuoteJpaEntity();
        entity.setId(domain.getId());
        entity.setQuoteNumber(domain.getQuoteNumber().value());
        entity.setCustomerId(domain.getCustomerId());
        entity.setCustomerName(domain.getCustomerName());
        entity.setCustomerRif(domain.getCustomerRif());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedByName(domain.getCreatedByName());
        entity.setStatus(domain.getStatus().name());
        entity.setExpiryDate(domain.getExpiryDate());
        entity.setNotes(domain.getNotes());
        entity.setSubtotal(domain.getSubtotal());
        entity.setOrderId(domain.getOrderId());
        entity.setOrderNumber(domain.getOrderNumber());
        entity.setInvoiceId(domain.getInvoiceId());
        entity.setInvoiceNumber(domain.getInvoiceNumber());
        entity.setCancelledAt(domain.getCancelledAt());
        entity.setCancelledReason(domain.getCancelledReason());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<QuoteLineJpaEntity> lineEntities = domain.getLines().stream()
                .map(line -> toLineJpa(line, entity)).toList();
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        return entity;
    }

    public Quote toDomain(QuoteJpaEntity entity) {
        List<QuoteLine> lines = entity.getLines().stream()
                .map(this::toLineDomain).toList();

        return Quote.reconstitute(entity.getId(),
                new QuoteNumber(entity.getQuoteNumber()),
                entity.getCustomerId(), entity.getCustomerName(),
                entity.getCustomerRif(), entity.getCreatedBy(),
                entity.getCreatedByName(),
                QuoteStatus.valueOf(entity.getStatus()), entity.getExpiryDate(),
                entity.getNotes(), entity.getSubtotal(), entity.getOrderId(),
                entity.getOrderNumber(), entity.getInvoiceId(),
                entity.getInvoiceNumber(), entity.getCancelledAt(),
                entity.getCancelledReason(), lines, entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private QuoteLineJpaEntity toLineJpa(QuoteLine line,
            QuoteJpaEntity quoteEntity) {
        QuoteLineJpaEntity entity = new QuoteLineJpaEntity();
        entity.setId(line.getId().value());
        entity.setQuote(quoteEntity);
        entity.setProductId(line.getProductId());
        entity.setProductName(line.getProductName());
        entity.setProductCode(line.getProductCode());
        entity.setQuantity(line.getQuantity());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getLineTotal());
        entity.setCategorySnapshot(line.getCategoryId());

        List<QuoteAllocationJpaEntity> allocationEntities = line
                .getAllocations().stream()
                .map(a -> toAllocationJpa(a, entity, quoteEntity.getId()))
                .toList();
        entity.getAllocations().clear();
        entity.getAllocations().addAll(allocationEntities);

        return entity;
    }

    private QuoteAllocationJpaEntity toAllocationJpa(QuoteAllocation allocation,
            QuoteLineJpaEntity lineEntity, java.util.UUID quoteId) {
        QuoteAllocationJpaEntity entity = new QuoteAllocationJpaEntity();
        entity.setId(allocation.getId().value());
        entity.setQuoteLine(lineEntity);
        entity.setQuoteId(quoteId);
        entity.setProductId(allocation.getProductId());
        entity.setWarehouseId(allocation.getWarehouseId());
        entity.setWarehouseName(allocation.getWarehouseName());
        entity.setAllocatedQuantity(allocation.getAllocatedQuantity());
        return entity;
    }

    private QuoteLine toLineDomain(QuoteLineJpaEntity entity) {
        List<QuoteAllocation> allocations = entity.getAllocations().stream()
                .map(a -> QuoteAllocation.reconstitute(
                        QuoteAllocationId.of(a.getId()), a.getProductId(),
                        a.getWarehouseId(), a.getWarehouseName(),
                        a.getAllocatedQuantity()))
                .toList();

        return QuoteLine.reconstitute(QuoteLineId.of(entity.getId()),
                entity.getProductId(), entity.getProductName(),
                entity.getProductCode(), entity.getCategoryIdSnapshot(),
                entity.getQuantity(), entity.getUnitPrice(),
                entity.getLineTotal(), allocations);
    }
}

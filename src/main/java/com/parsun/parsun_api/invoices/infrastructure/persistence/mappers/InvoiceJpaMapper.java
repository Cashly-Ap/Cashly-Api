package com.parsun.parsun_api.invoices.infrastructure.persistence.mappers;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceLineId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceReturnStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceLineAllocationEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceLineEntity;

import java.util.List;

public class InvoiceJpaMapper {

    private InvoiceJpaMapper() {}

    public static InvoiceEntity toEntity(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(invoice.getId().value());
        entity.setInvoiceNumber(invoice.getInvoiceNumber().value());
        entity.setCustomerId(invoice.getCustomerId());
        entity.setCustomerName(invoice.getCustomerName());
        entity.setCustomerRif(invoice.getCustomerRif());
        entity.setCreatedBy(invoice.getCreatedBy());
        entity.setCreatedByName(invoice.getCreatedByName());
        entity.setOrderId(invoice.getOrderId());
        entity.setOrderNumber(invoice.getOrderNumber());
        entity.setOrderIds(invoice.getOrderIds());
        entity.setQuoteIds(invoice.getQuoteIds());
        entity.setStatus(invoice.getStatus().name());
        entity.setPaymentType(invoice.getPaymentType().name());
        entity.setNotes(invoice.getNotes());
        entity.setSubtotal(invoice.getSubtotal());
        entity.setReturnedTotal(invoice.getReturnedTotal());
        entity.setReturnStatus(invoice.getReturnStatus() != null ? invoice.getReturnStatus().name() : null);
        entity.setCancelledAt(invoice.getCancelledAt());
        entity.setCancelledReason(invoice.getCancelledReason());
        entity.setCreatedAt(invoice.getCreatedAt());
        entity.setUpdatedAt(invoice.getUpdatedAt());
        entity.setIssueDate(invoice.getIssueDate());
        entity.setDueDate(invoice.getDueDate());

        List<InvoiceLineEntity> lineEntities = invoice.getLines().stream()
                .map(line -> toLineEntity(line, entity))
                .toList();
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        return entity;
    }

    public static Invoice toDomain(InvoiceEntity entity) {
        List<InvoiceLine> lines = entity.getLines().stream()
                .map(InvoiceJpaMapper::toLineDomain)
                .toList();

        InvoiceReturnStatus returnStatus = entity.getReturnStatus() != null
                ? InvoiceReturnStatus.valueOf(entity.getReturnStatus())
                : null;

        return Invoice.reconstitute(
                InvoiceId.of(entity.getId()),
                new InvoiceNumber(entity.getInvoiceNumber()),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getCustomerRif(),
                entity.getCreatedBy(),
                entity.getCreatedByName(),
                entity.getOrderId(),
                entity.getOrderNumber(),
                entity.getOrderIds(),
                entity.getQuoteIds(),
                InvoiceStatus.valueOf(entity.getStatus()),
                PaymentType.valueOf(entity.getPaymentType()),
                entity.getNotes(),
                entity.getReturnedTotal(),
                returnStatus,
                entity.getCancelledAt(),
                entity.getCancelledReason(),
                lines,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIssueDate(),
                entity.getDueDate()
        );
    }

    private static InvoiceLineEntity toLineEntity(InvoiceLine line, InvoiceEntity invoiceEntity) {
        InvoiceLineEntity entity = new InvoiceLineEntity();
        entity.setId(line.getId().value());
        entity.setInvoice(invoiceEntity);
        entity.setProductId(line.getProductId());
        entity.setProductName(line.getProductName());
        entity.setProductCode(line.getProductCode());
        entity.setQuantity(line.getQuantity());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getLineTotal());
        entity.setReturnedQuantity(line.getReturnedQuantity());

        List<InvoiceLineAllocationEntity> allocationEntities = line.getAllocations().stream()
                .map(a -> toAllocationEntity(a, entity))
                .toList();
        entity.getAllocations().clear();
        entity.getAllocations().addAll(allocationEntities);

        return entity;
    }

    private static InvoiceLineAllocationEntity toAllocationEntity(InvoiceLineAllocation allocation,
                                                                    InvoiceLineEntity lineEntity) {
        InvoiceLineAllocationEntity entity = new InvoiceLineAllocationEntity();
        entity.setId(allocation.getId());
        entity.setInvoiceLine(lineEntity);
        entity.setWarehouseId(allocation.getWarehouseId());
        entity.setAllocated(allocation.getAllocated());
        return entity;
    }

    private static InvoiceLine toLineDomain(InvoiceLineEntity entity) {
        List<InvoiceLineAllocation> allocations = entity.getAllocations().stream()
                .map(a -> InvoiceLineAllocation.reconstitute(a.getId(), a.getWarehouseId(), a.getAllocated()))
                .toList();

        return InvoiceLine.reconstitute(
                InvoiceLineId.of(entity.getId()),
                entity.getProductId(),
                entity.getProductName(),
                entity.getProductCode(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getReturnedQuantity(),
                allocations
        );
    }
}

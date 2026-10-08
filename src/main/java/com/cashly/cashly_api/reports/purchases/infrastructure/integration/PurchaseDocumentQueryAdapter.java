package com.cashly.cashly_api.reports.purchases.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.api.PurchaseDocumentQuery;
import com.cashly.cashly_api.purchases.application.api.PurchaseDocumentFilter;
import com.cashly.cashly_api.reports.purchases.application.contracts.PurchaseReportQueryGateway;
import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportData;
import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportRow;
import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportFilter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class PurchaseDocumentQueryAdapter implements PurchaseReportQueryGateway {
    private final PurchaseDocumentQuery query;
    public PurchaseDocumentQueryAdapter(PurchaseDocumentQuery query) { this.query = query; }
    public PurchaseReportData find(UUID id) {
        var data = query.find(id);
        return new PurchaseReportData(data.id(), data.purchaseNumber(), data.supplierName(), data.supplierRif(), data.status(),
                data.notes(), data.cancelledReason(), data.createdAt(), data.subtotal(),
                data.lines().stream().map(line -> new PurchaseReportData.Line(line.productCode(), line.productName(), line.quantity(), line.unitCost(), line.subtotal())).toList());
    }
    public List<PurchaseReportRow> findAll(PurchaseReportFilter filter) {
        return query.findAll(new PurchaseDocumentFilter(filter.dateFrom(), filter.dateTo(), filter.supplierId(), filter.status(), filter.productId(), filter.categoryId()))
                .stream().map(data -> new PurchaseReportRow(data.id(), data.purchaseNumber(), data.supplierName(), data.status(), data.subtotal(), data.createdAt())).toList();
    }
}
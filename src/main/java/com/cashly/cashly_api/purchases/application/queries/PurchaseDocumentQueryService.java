package com.cashly.cashly_api.purchases.application.queries;

import com.cashly.cashly_api.purchases.application.ports.in.PurchaseDocumentQuery;
import com.cashly.cashly_api.purchases.application.ports.in.PurchaseDocumentSnapshot;
import com.cashly.cashly_api.purchases.application.ports.in.PurchaseDocumentRow;
import com.cashly.cashly_api.purchases.application.ports.in.PurchaseDocumentFilter;
import com.cashly.cashly_api.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.purchases.application.list.ListPurchasesUseCase;
import com.cashly.cashly_api.purchases.domain.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;

import java.util.List;
import java.util.UUID;

public class PurchaseDocumentQueryService implements PurchaseDocumentQuery {
    private final PurchaseRepository repository;
    private final ListPurchasesUseCase listQuery;
    public PurchaseDocumentQueryService(PurchaseRepository repository, ListPurchasesUseCase listQuery) {
        this.repository = repository;
        this.listQuery = listQuery;
    }
    public PurchaseDocumentSnapshot find(UUID id) {
        var entity = repository.findById(com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId.of(id)).orElseThrow(() -> new PurchaseNotFoundException(id));
        var data = PurchaseMapper.toDetailResponse(entity);
        return new PurchaseDocumentSnapshot(data.id(), data.purchaseNumber(), data.supplierName(), data.supplierRif(), data.status(),
                data.notes(), data.cancelledReason(), data.createdAt(), data.subtotal(),
                data.lines().stream().map(line -> new PurchaseDocumentSnapshot.Line(line.productCode(), line.productName(), line.quantity(), line.unitCost(), line.subtotal())).toList());
    }
    public List<PurchaseDocumentRow> findAll(PurchaseDocumentFilter filter) {
        return listQuery.execute(filter.dateFrom(), filter.dateTo(), filter.supplierId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc")
                .stream().map(data -> new PurchaseDocumentRow(data.id(), data.purchaseNumber(), data.supplierName(), data.status(), data.subtotal(), data.createdAt())).toList();
    }
}
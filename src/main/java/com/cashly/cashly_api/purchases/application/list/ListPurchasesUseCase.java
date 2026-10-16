package com.cashly.cashly_api.purchases.application.list;

import com.cashly.cashly_api.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.purchases.domain.PurchaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ListPurchasesUseCase {

    private final PurchaseRepository purchaseRepository;

    public ListPurchasesUseCase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public List<PurchaseListResponse> execute(LocalDate fromDate, LocalDate toDate,
                                                UUID supplierId, String status,
                                                UUID productId, UUID categoryId,
                                                String orderBy, String orderDir) {
        return purchaseRepository.findAllWithFilters(fromDate, toDate, supplierId, status, productId, categoryId, orderBy, orderDir)
                .stream()
                .map(PurchaseMapper::toListResponse)
                .toList();
    }
}

package com.cashly.cashly_api.procurement.purchases.application.list.use_case;

import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.procurement.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;

import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ListPurchasesUseCase {

    private final PurchaseRepository purchaseRepository;

    public ListPurchasesUseCase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public List<PurchaseListResponse> execute(LocalDate fromDate,
            LocalDate toDate, UUID supplierId, String status, UUID productId,
            UUID categoryId, String orderBy, String orderDir) {
        return purchaseRepository
                .findAllWithFilters(fromDate, toDate, supplierId, status,
                        productId, categoryId, orderBy, orderDir)
                .stream().map(PurchaseMapper::toListResponse).toList();
    }
    public List<PurchaseListResponse> execute(LocalDate fromDate,
            LocalDate toDate, UUID supplierId, String status, UUID productId,
            UUID categoryId, String orderBy, String orderDir,
            DocumentAction action) {
        if (action == null)
            return execute(fromDate, toDate, supplierId, status, productId,
                    categoryId, orderBy, orderDir);
        String eligibleStatus = switch (action) {
            case EDIT, CONFIRM -> "DRAFT";
            case CANCEL -> "CONFIRMED";
            default -> throw new IllegalArgumentException(
                    "Unsupported purchase action: " + action);
        };
        if (status != null && !status.isBlank() && !"ALL".equals(status)
                && !eligibleStatus.equals(status)) {
            return List.of();
        }
        return execute(fromDate, toDate, supplierId, eligibleStatus, productId,
                categoryId, orderBy, orderDir);
    }

}

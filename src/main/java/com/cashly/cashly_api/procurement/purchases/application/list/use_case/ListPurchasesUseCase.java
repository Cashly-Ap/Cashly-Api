package com.cashly.cashly_api.procurement.purchases.application.list.use_case;

import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.procurement.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;

import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ListPurchasesUseCase {

    private static final List<String> DEFAULT_STATUSES = List.of("DRAFT");
    private static final List<String> CANCELLABLE_STATUSES = List.of("DRAFT",
            "CONFIRMED");
    private static final List<String> ALL_STATUSES = List.of("DRAFT",
            "CONFIRMED", "CANCELLED");

    private final PurchaseRepository purchaseRepository;

    public ListPurchasesUseCase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public List<PurchaseListResponse> execute(LocalDate fromDate,
            LocalDate toDate, UUID supplierId, String status, UUID productId,
            UUID categoryId, String orderBy, String orderDir) {
        return query(fromDate, toDate, supplierId, statuses(status), productId,
                categoryId, orderBy, orderDir);
    }

    public List<PurchaseListResponse> execute(LocalDate fromDate,
            LocalDate toDate, UUID supplierId, String status, UUID productId,
            UUID categoryId, String orderBy, String orderDir,
            DocumentAction action) {
        if (action == null)
            return execute(fromDate, toDate, supplierId, status, productId,
                    categoryId, orderBy, orderDir);
        var eligibleStatuses = switch (action) {
            case EDIT, CONFIRM, DELETE -> DEFAULT_STATUSES;
            case CANCEL -> CANCELLABLE_STATUSES;
            default -> throw new IllegalArgumentException(
                    "Unsupported purchase action: " + action);
        };
        if (status != null && !status.isBlank() && !"ALL".equals(status)
                && !eligibleStatuses.contains(status)) {
            return List.of();
        }
        return query(fromDate, toDate, supplierId, eligibleStatuses, productId,
                categoryId, orderBy, orderDir);
    }

    private List<PurchaseListResponse> query(LocalDate fromDate,
            LocalDate toDate, UUID supplierId, Collection<String> statuses,
            UUID productId, UUID categoryId, String orderBy, String orderDir) {
        return purchaseRepository
                .findAllWithFilters(fromDate, toDate, supplierId, statuses,
                        productId, categoryId, orderBy, orderDir)
                .stream().map(PurchaseMapper::toListResponse).toList();
    }

    private Collection<String> statuses(String status) {
        if (status == null || status.isBlank())
            return DEFAULT_STATUSES;
        if ("ALL".equals(status))
            return ALL_STATUSES;
        return List.of(status);
    }

}

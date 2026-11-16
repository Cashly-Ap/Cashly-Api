package com.cashly.cashly_api.procurement.purchases.domain.repository;

import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.Set;

public interface PurchaseRepository {
    Purchase save(Purchase purchase);
    Optional<Purchase> findById(PurchaseId id);
    List<Purchase> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID supplierId, Collection<String> statuses, UUID productId,
            UUID categoryId, String orderBy, String orderDir);

    PageResponse<Purchase> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID supplierId, Collection<String> statuses,
            String search, UUID productId, UUID categoryId, int page, int size,
            String orderBy, String orderDir);
    int getNextSequenceForYear(int year);
    void deleteById(PurchaseId id);
    boolean existsBySupplierId(UUID supplierId);
    Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds);
}

package com.parsun.parsun_api.purchases.domain.repositories;

import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository {
    Purchase save(Purchase purchase);
    Optional<Purchase> findById(PurchaseId id);
    List<Purchase> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                       UUID supplierId, String status,
                                       UUID productId, UUID categoryId,
                                       String orderBy, String orderDir);
    int getNextSequenceForYear(int year);
    void deleteById(PurchaseId id);
}

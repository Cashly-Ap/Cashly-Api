package com.cashly.cashly_api.purchases.infrastructure.adapters.out.persistence.adapters;

import com.cashly.cashly_api.purchases.infrastructure.adapters.out.persistence.repositories.PurchaseJpaRepository;
import com.cashly.cashly_api.purchases.infrastructure.adapters.out.persistence.repositories.PurchaseNumberSequenceJpaRepository;

import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.infrastructure.adapters.out.persistence.entities.PurchaseEntity;
import com.cashly.cashly_api.purchases.infrastructure.adapters.out.persistence.entities.PurchaseNumberSequenceEntity;
import com.cashly.cashly_api.purchases.infrastructure.adapters.out.persistence.mappers.PurchaseJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.Set;

@Repository
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private final PurchaseJpaRepository jpaRepository;
    private final PurchaseNumberSequenceJpaRepository sequenceJpaRepository;

    public PurchaseRepositoryAdapter(PurchaseJpaRepository jpaRepository,
                                      PurchaseNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = PurchaseJpaMapper.toEntity(purchase);
        PurchaseEntity saved = jpaRepository.save(entity);
        return PurchaseJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Purchase> findById(PurchaseId id) {
        Optional<PurchaseEntity> found = jpaRepository.findWithLinesById(id.value());
        found.ifPresent(p -> jpaRepository.findWithAllocationsForPurchase(id.value()));
        return found.map(PurchaseJpaMapper::toDomain);
    }

    @Override
    public List<Purchase> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                              UUID supplierId, String status,
                                              UUID productId, UUID categoryId,
                                              String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<PurchaseEntity> entities = jpaRepository.findAllWithFilters(from, to, supplierId, status, productId, categoryId);

        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<PurchaseEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(PurchaseEntity::getCreatedAt);
            case "purchaseNumber" -> Comparator.comparing(PurchaseEntity::getPurchaseNumber);
            case "supplierName" -> Comparator.comparing(PurchaseEntity::getSupplierName);
            case "status" -> Comparator.comparing(PurchaseEntity::getStatus);
            case "subtotal" -> Comparator.comparing(PurchaseEntity::getSubtotal);
            default -> Comparator.comparing(PurchaseEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream()
                .sorted(comparator)
                .map(PurchaseJpaMapper::toDomain)
                .toList();
    }

    @Override
    public int getNextSequenceForYear(int year) {
        PurchaseNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    PurchaseNumberSequenceEntity newSeq = new PurchaseNumberSequenceEntity();
                    newSeq.setYear(year);
                    newSeq.setNextValue(0);
                    return newSeq;
                });

        int next = sequence.getNextValue() + 1;
        sequence.setNextValue(next);
        sequenceJpaRepository.save(sequence);
        return next;
    }

    @Override
    public void deleteById(PurchaseId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsBySupplierId(UUID supplierId) {
        return jpaRepository.existsBySupplierId(supplierId);
    }

    @Override
    public Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds) {
        if (supplierIds.isEmpty()) {
            return Set.of();
        }
        return jpaRepository.findSupplierIdsWithPurchases(supplierIds);
    }
}

package com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.billing.returns.domain.entity.CancelledAllocation;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnId;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.ReturnLine;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.ReturnLineAllocation;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnNumber;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnStatus;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnType;
import com.cashly.cashly_api.finance.billing.returns.domain.repository.ReturnRepository;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnCancelledAllocationEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnLineAllocationEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnLineEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnNumberSequenceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaReturnRepository implements ReturnRepository {

    private final EntityManager entityManager;

    public JpaReturnRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Return save(Return return_) {
        ReturnEntity entity = toEntity(return_);
        if (return_.getId() == null
                || findJpaEntity(return_.getId().value()).isEmpty()) {
            entityManager.persist(entity);
        } else {
            entity = entityManager.merge(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Return> findById(ReturnId id) {
        return findJpaEntity(id.value()).map(this::toDomain);
    }

    @Override
    public List<Return> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, UUID invoiceId) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT r FROM ReturnEntity r
                LEFT JOIN FETCH r.lines l
                WHERE 1=1
                """);

        if (dateFrom != null) {
            jpql.append(" AND r.createdAt >= :dateFrom");
        }
        if (dateTo != null) {
            jpql.append(" AND r.createdAt <= :dateTo");
        }
        if (customerId != null) {
            jpql.append(" AND r.customerId = :customerId");
        }
        if (invoiceId != null) {
            jpql.append(" AND r.invoiceId = :invoiceId");
        }
        jpql.append(" ORDER BY r.createdAt DESC");

        TypedQuery<ReturnEntity> query = entityManager
                .createQuery(jpql.toString(), ReturnEntity.class);

        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom.atStartOfDay());
        }
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo.atTime(23, 59, 59));
        }
        if (customerId != null) {
            query.setParameter("customerId", customerId);
        }
        if (invoiceId != null) {
            query.setParameter("invoiceId", invoiceId);
        }

        return query.getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(ReturnId id) {
        findJpaEntity(id.value()).ifPresent(entityManager::remove);
    }

    @Override
    public int getNextSequenceForYear(int year) {
        ReturnNumberSequenceEntity sequence = entityManager.find(
                ReturnNumberSequenceEntity.class, year,
                LockModeType.PESSIMISTIC_WRITE);

        if (sequence == null) {
            sequence = new ReturnNumberSequenceEntity();
            sequence.setYear(year);
            sequence.setNextValue(0);
        }

        int next = sequence.getNextValue() + 1;
        sequence.setNextValue(next);
        entityManager.merge(sequence);
        return next;
    }

    private Optional<ReturnEntity> findJpaEntity(UUID id) {
        ReturnEntity entity = entityManager.find(ReturnEntity.class, id);
        if (entity != null) {
            entity.getLines().forEach(line -> line.getAllocations().size());
        }
        return Optional.ofNullable(entity);
    }

    private ReturnEntity toEntity(Return return_) {
        ReturnEntity entity = new ReturnEntity();
        entity.setId(return_.getId().value());
        entity.setReturnNumber(return_.getReturnNumber().value());
        entity.setInvoiceId(return_.getInvoiceId());
        entity.setCustomerId(return_.getCustomerId());
        entity.setReturnType(return_.getReturnType().name());
        entity.setReason(return_.getReason());
        entity.setTotal(return_.getTotal());
        entity.setCreatedBy(return_.getCreatedBy());
        entity.setCreatedByName(return_.getCreatedByName());
        entity.setCreatedAt(return_.getCreatedAt());
        entity.setUpdatedAt(return_.getUpdatedAt());

        List<ReturnLineEntity> lineEntities = return_.getLines().stream()
                .map(line -> toLineEntity(line, entity)).toList();
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        List<ReturnCancelledAllocationEntity> cancelledEntities = return_
                .getCancelledAllocations().stream()
                .map(ca -> toCancelledAllocationEntity(ca, entity)).toList();
        entity.getCancelledAllocations().clear();
        entity.getCancelledAllocations().addAll(cancelledEntities);

        return entity;
    }

    private ReturnLineEntity toLineEntity(ReturnLine line,
            ReturnEntity returnEntity) {
        ReturnLineEntity entity = new ReturnLineEntity();
        entity.setId(UUID.randomUUID());
        entity.setReturnEntity(returnEntity);
        entity.setInvoiceLineId(line.getInvoiceLineId());
        entity.setProductId(line.getProductId());
        entity.setQuantity(line.getQuantity());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getSubtotal());

        List<ReturnLineAllocationEntity> allocationEntities = line
                .getAllocations().stream()
                .map(alloc -> toAllocationEntity(alloc, entity)).toList();
        entity.getAllocations().clear();
        entity.getAllocations().addAll(allocationEntities);

        return entity;
    }

    private ReturnLineAllocationEntity toAllocationEntity(
            ReturnLineAllocation allocation, ReturnLineEntity returnLine) {
        ReturnLineAllocationEntity entity = new ReturnLineAllocationEntity();
        entity.setId(allocation.getId());
        entity.setReturnLine(returnLine);
        entity.setWarehouseId(allocation.getWarehouseId());
        entity.setAllocated(allocation.getAllocated());
        return entity;
    }

    private Return toDomain(ReturnEntity entity) {
        List<ReturnLine> lines = entity.getLines().stream()
                .map(this::toLineDomain).toList();

        List<CancelledAllocation> cancelledAllocations = entity
                .getCancelledAllocations().stream()
                .map(this::toCancelledAllocationDomain).toList();

        return Return.reconstitute(ReturnId.of(entity.getId()),
                new ReturnNumber(entity.getReturnNumber()),
                entity.getInvoiceId(), entity.getCustomerId(),
                ReturnType.valueOf(entity.getReturnType()), entity.getReason(),
                entity.getTotal(), lines, cancelledAllocations,
                entity.getCreatedBy(), entity.getCreatedByName(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private ReturnLine toLineDomain(ReturnLineEntity entity) {
        List<ReturnLineAllocation> allocations = entity.getAllocations()
                .stream().map(this::toAllocationDomain).toList();

        return ReturnLine.create(entity.getInvoiceLineId(),
                entity.getProductId(), entity.getQuantity(),
                entity.getUnitPrice(), entity.getLineTotal(), allocations);
    }

    private ReturnLineAllocation toAllocationDomain(
            ReturnLineAllocationEntity entity) {
        return ReturnLineAllocation.reconstitute(entity.getId(),
                entity.getWarehouseId(), entity.getAllocated());
    }

    private ReturnCancelledAllocationEntity toCancelledAllocationEntity(
            CancelledAllocation ca, ReturnEntity returnEntity) {
        ReturnCancelledAllocationEntity entity = new ReturnCancelledAllocationEntity();
        entity.setId(UUID.randomUUID());
        entity.setReturnEntity(returnEntity);
        entity.setPaymentId(ca.getPaymentId());
        entity.setInvoiceId(ca.getInvoiceId());
        entity.setAmount(ca.getAmount());
        return entity;
    }

    private CancelledAllocation toCancelledAllocationDomain(
            ReturnCancelledAllocationEntity entity) {
        return CancelledAllocation.reconstitute(entity.getPaymentId(),
                entity.getInvoiceId(), entity.getAmount());
    }
}

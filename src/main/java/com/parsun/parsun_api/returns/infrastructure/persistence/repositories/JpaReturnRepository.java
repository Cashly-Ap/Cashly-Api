package com.parsun.parsun_api.returns.infrastructure.persistence.repositories;

import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnId;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnLineAllocation;
import com.parsun.parsun_api.returns.domain.entities.ReturnNumber;
import com.parsun.parsun_api.returns.domain.entities.ReturnStatus;
import com.parsun.parsun_api.returns.domain.entities.ReturnType;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.returns.infrastructure.persistence.entities.ReturnEntity;
import com.parsun.parsun_api.returns.infrastructure.persistence.entities.ReturnLineAllocationEntity;
import com.parsun.parsun_api.returns.infrastructure.persistence.entities.ReturnLineEntity;
import com.parsun.parsun_api.returns.infrastructure.persistence.entities.ReturnNumberSequenceEntity;
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
        if (return_.getId() == null || findJpaEntity(return_.getId().value()).isEmpty()) {
            entityManager.persist(entity);
        } else {
            entity = entityManager.merge(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Return> findById(ReturnId id) {
        return findJpaEntity(id.value())
                .map(this::toDomain);
    }

    @Override
    public List<Return> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                            UUID customerId, UUID invoiceId) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT r FROM ReturnEntity r
                LEFT JOIN FETCH r.lines l
                LEFT JOIN FETCH l.allocations
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

        TypedQuery<ReturnEntity> query = entityManager.createQuery(jpql.toString(), ReturnEntity.class);

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

        return query.getResultList().stream()
                .map(this::toDomain)
                .toList();
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
        entity.setInvoiceId(return_.getInvoiceId().value());
        entity.setCustomerId(return_.getCustomerId());
        entity.setCustomerName(return_.getCustomerName());
        entity.setCustomerRif(return_.getCustomerRif());
        entity.setReturnType(return_.getReturnType().name());
        entity.setReason(return_.getReason());
        entity.setTotal(return_.getTotal());
        entity.setCreatedBy(return_.getCreatedBy());
        entity.setCreatedByName(return_.getCreatedByName());
        entity.setCreatedAt(return_.getCreatedAt());
        entity.setUpdatedAt(return_.getUpdatedAt());

        List<ReturnLineEntity> lineEntities = return_.getLines().stream()
                .map(line -> toLineEntity(line, entity))
                .toList();
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        return entity;
    }

    private ReturnLineEntity toLineEntity(ReturnLine line, ReturnEntity returnEntity) {
        ReturnLineEntity entity = new ReturnLineEntity();
        entity.setId(UUID.randomUUID());
        entity.setReturnEntity(returnEntity);
        entity.setProductId(line.getProductId());
        entity.setQuantity(line.getQuantity());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getSubtotal());

        List<ReturnLineAllocationEntity> allocationEntities = line.getAllocations().stream()
                .map(alloc -> toAllocationEntity(alloc, entity))
                .toList();
        entity.getAllocations().clear();
        entity.getAllocations().addAll(allocationEntities);

        return entity;
    }

    private ReturnLineAllocationEntity toAllocationEntity(ReturnLineAllocation allocation, ReturnLineEntity returnLine) {
        ReturnLineAllocationEntity entity = new ReturnLineAllocationEntity();
        entity.setId(allocation.getId());
        entity.setReturnLine(returnLine);
        entity.setWarehouseId(allocation.getWarehouseId());
        entity.setAllocated(allocation.getAllocated());
        return entity;
    }

    private Return toDomain(ReturnEntity entity) {
        List<ReturnLine> lines = entity.getLines().stream()
                .map(this::toLineDomain)
                .toList();

        return Return.reconstitute(
                ReturnId.of(entity.getId()),
                new ReturnNumber(entity.getReturnNumber()),
                InvoiceId.of(entity.getInvoiceId()),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getCustomerRif(),
                ReturnType.valueOf(entity.getReturnType()),
                entity.getReason(),
                entity.getTotal(),
                lines,
                entity.getCreatedBy(),
                entity.getCreatedByName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ReturnLine toLineDomain(ReturnLineEntity entity) {
        List<ReturnLineAllocation> allocations = entity.getAllocations().stream()
                .map(this::toAllocationDomain)
                .toList();

        return ReturnLine.create(
                entity.getProductId(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getLineTotal(),
                allocations
        );
    }

    private ReturnLineAllocation toAllocationDomain(ReturnLineAllocationEntity entity) {
        return ReturnLineAllocation.reconstitute(
                entity.getId(),
                entity.getWarehouseId(),
                entity.getAllocated()
        );
    }
}
